#
# Copyright (c) 2026 Emerson Electric Co.
#
# SPDX-License-Identifier: MIT
#
"""Fetch verified feed package CycloneDX SBOMs and add their inventory to
OpenEmbedded SPDX 3.0 image documents.

NI packages installed from feeds have no recipe in the build, so OE-core
cannot generate SPDX for them. Each package's build publishes
<pkg>_<ver>_<arch>.ipk.cdx.sbom.json next to the ipk in the NI package pool:

    <pool>/<prefix>/<pkg>/<X.Y.Z>/<ver>/<build-hash>/<pkg>_<ver>_<arch>.ipk.cdx.sbom.json

The downloader has no BitBake dependency. Conversion uses OE's SPDX model.
"""

import base64
import html.parser
import json
import os
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

SBOM_SUFFIX = ".ipk.cdx.sbom.json"

class FeedSbomError(Exception):
    pass


def sbom_filename(name, version, arch):
    return "%s_%s_%s%s" % (name, version, arch, SBOM_SUFFIX)


def version_series(version):
    """Return the X.Y.Z pool folder for a version such as 27.0.0.60-0+d60."""
    upstream = version.split(":", 1)[-1].split("-", 1)[0]
    parts = upstream.split(".")
    if len(parts) < 3 or not all(p.isdigit() for p in parts[:3]):
        raise FeedSbomError("Cannot derive the X.Y.Z pool folder from version '%s'" % version)
    return ".".join(parts[:3])


def version_dir_url(pool_uri, prefix, name, version):
    q = urllib.parse.quote
    return "%s/%s/%s/%s/%s/" % (pool_uri.rstrip("/"), q(prefix), q(name),
                                q(version_series(version)), q(version, safe=""))


class _LinkParser(html.parser.HTMLParser):
    def __init__(self):
        super().__init__()
        self.links = []

    def handle_starttag(self, tag, attrs):
        if tag.lower() == "a":
            for key, value in attrs:
                if key.lower() == "href" and value:
                    self.links.append(value)


def list_subdirs(index_html, base_url):
    """Return names of the direct sub-folders of base_url in an HTTP index page."""
    parser = _LinkParser()
    parser.feed(index_html)
    base_path = urllib.parse.unquote(urllib.parse.urlsplit(base_url).path).rstrip("/") + "/"
    names = []
    for link in parser.links:
        path = urllib.parse.unquote(urllib.parse.urlsplit(urllib.parse.urljoin(base_url, link)).path)
        if not path.startswith(base_path) or path == base_path:
            continue
        rest = path[len(base_path):].strip("/")
        if rest and "/" not in rest and rest not in names:
            names.append(rest)
    return names


def http_get(url, timeout=60, retries=3):
    """Return the body of url, or None if the server answers 404."""
    last = None
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(url, timeout=timeout) as resp:
                return resp.read()
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return None
            last = e
        except (urllib.error.URLError, OSError) as e:
            last = e
        time.sleep(2 * attempt)
    raise FeedSbomError("Failed to download %s: %s" % (url, last))


def _prefixes(pool_uri, name, get):
    """Yield candidate pool prefix folders for name, most likely first."""
    yield name[:4]
    root_url = pool_uri.rstrip("/") + "/"
    root = get(root_url)
    if root is not None:
        tops = list_subdirs(root.decode("utf-8", "replace"), root_url)
        for top in sorted(tops, key=len, reverse=True):
            if name.startswith(top) and top != name[:4]:
                yield top


def _verified_sbom(data, source, name, version, arch, md5):
    try:
        cdx = json.loads(data)
    except ValueError as e:
        raise FeedSbomError("%s is not valid JSON: %s" % (source, e))
    if not isinstance(cdx, dict):
        raise FeedSbomError("%s is not a CycloneDX document object" % source)
    problems = verify_sbom(cdx, name, version, arch, md5)
    if problems:
        raise FeedSbomError("%s does not match the installed package: %s" % (
            source, "; ".join(problems)))


def _find_sbom(pool_uri, name, version, arch, get, md5):
    """Return the URL and bytes of a verified package SBOM.

    The build-hash folder cannot be derived from the package, so each
    sub-folder is tried until the SBOM matches the installed package.
    """
    fname = sbom_filename(name, version, arch)
    tried = []
    rejected = []
    for prefix in _prefixes(pool_uri, name, get):
        vdir = version_dir_url(pool_uri, prefix, name, version)
        tried.append(vdir)
        index = get(vdir)
        if index is None:
            continue
        for build_hash in list_subdirs(index.decode("utf-8", "replace"), vdir):
            url = vdir + urllib.parse.quote(build_hash) + "/" + urllib.parse.quote(fname)
            data = get(url)
            if data is None:
                continue
            try:
                _verified_sbom(data, url, name, version, arch, md5)
            except FeedSbomError as e:
                rejected.append(str(e))
                continue
            return url, data
    details = "\nRejected candidates:\n" + "\n".join(rejected) if rejected else ""
    raise FeedSbomError("No %s found matching the installed package in the NI package pool "
                        "(looked in: %s)%s" % (fname, ", ".join(tried), details))


def find_sbom_url(pool_uri, name, version, arch, get=http_get, md5=None):
    """Locate an SBOM matching the package identity and expected feed MD5."""
    url, _ = _find_sbom(pool_uri, name, version, arch, get, md5)
    return url


def fetch_sbom(pool_uri, cache_dir, name, version, arch, allow_network=True, get=http_get, md5=None):
    """Return (path, source_url), caching only an SBOM verified against the package."""
    fname = sbom_filename(name, version, arch)
    path = os.path.join(cache_dir, fname)
    url_file = path + ".url"
    cache_error = None
    if os.path.exists(path):
        with open(path, "rb") as f:
            data = f.read()
        try:
            _verified_sbom(data, path, name, version, arch, md5)
        except FeedSbomError as e:
            cache_error = e
    if os.path.exists(path) and cache_error is None:
        source = None
        if os.path.exists(url_file):
            with open(url_file) as f:
                source = f.read().strip() or None
        return path, source

    if not allow_network:
        if cache_error:
            raise FeedSbomError("%s; network access is disabled (BB_NO_NETWORK)" % cache_error)
        raise FeedSbomError("%s is not in %s and network access is disabled (BB_NO_NETWORK)" % (fname, cache_dir))

    try:
        url, data = _find_sbom(pool_uri, name, version, arch, get, md5)
    except FeedSbomError as e:
        if cache_error:
            raise FeedSbomError("Cached SBOM rejected: %s\n%s" % (cache_error, e)) from e
        raise

    os.makedirs(cache_dir, exist_ok=True)
    fd, tmp = tempfile.mkstemp(dir=cache_dir, prefix=".%s." % fname)
    with os.fdopen(fd, "wb") as f:
        f.write(data)
    with open(url_file, "w") as f:
        f.write(url + "\n")
    os.replace(tmp, path)
    return path, url


def parse_opkg_lists(lists_dir):
    """Return {(package, version, arch): md5} from the opkg feed lists in lists_dir."""
    result = {}
    if not lists_dir or not os.path.isdir(lists_dir):
        return result
    for entry in sorted(os.listdir(lists_dir)):
        path = os.path.join(lists_dir, entry)
        if not os.path.isfile(path):
            continue
        fields = {}
        with open(path, errors="replace") as f:
            for line in list(f) + [""]:
                line = line.rstrip("\n")
                if not line:
                    key = (fields.get("Package"), fields.get("Version"), fields.get("Architecture"))
                    if all(key) and fields.get("MD5Sum"):
                        md5 = fields["MD5Sum"].lower()
                        if key in result and result[key] != md5:
                            raise FeedSbomError("Conflicting feed IPK MD5 values for %s" % (key,))
                        result[key] = md5
                    fields = {}
                elif not line[0].isspace() and ":" in line:
                    k, v = line.split(":", 1)
                    fields[k] = v.strip()
    return result


def _purl_arch(purl):
    if not purl or "?" not in purl:
        return None
    query = urllib.parse.parse_qs(purl.split("?", 1)[1].split("#", 1)[0])
    return (query.get("architecture") or [None])[0]


def _hashes(component):
    return {h.get("alg", "").replace("-", "").upper(): h.get("content", "").lower()
            for h in component.get("hashes", []) if h.get("alg") and h.get("content")}


def verify_sbom(cdx, name, version, arch, md5=None):
    """Return a list of reasons why cdx does not describe the installed package."""
    problems = []
    if cdx.get("bomFormat") != "CycloneDX":
        problems.append("bomFormat is %r, expected 'CycloneDX'" % cdx.get("bomFormat"))
    comp = (cdx.get("metadata") or {}).get("component") or {}
    if comp.get("name") != name:
        problems.append("metadata.component.name is %r, expected %r" % (comp.get("name"), name))
    if comp.get("version") != version:
        problems.append("metadata.component.version is %r, expected %r" % (comp.get("version"), version))
    purl_arch = _purl_arch(comp.get("purl"))
    if not purl_arch:
        problems.append("metadata.component.purl has no architecture qualifier")
    elif purl_arch != arch:
        problems.append("purl architecture is %r, expected %r" % (purl_arch, arch))
    if md5:
        sbom_md5 = _hashes(comp).get("MD5")
        if not sbom_md5:
            problems.append("SBOM has no ipk MD5 to compare with the feed ipk %s" % md5.lower())
        elif sbom_md5 != md5.lower():
            problems.append("ipk MD5 in SBOM is %s but the feed ipk is %s" % (sbom_md5, md5.lower()))
    return problems


def cyclonedx_to_spdx30(objset, cdx, name, version, arch, license_data, source_url=None):
    """Add the package and both component trees to an OE SPDX 3.0 ObjectSet."""
    import oe.spdx30 as spdx

    problems = verify_sbom(cdx, name, version, arch)
    if problems:
        raise FeedSbomError("; ".join(problems))
    if cdx.get("services"):
        raise FeedSbomError("CycloneDX services are not supported for feed packages")
    references = {}
    ordinal = 0

    def add_licenses(package, entries):
        expressions = []
        for index, entry in enumerate(entries or []):
            license = entry.get("license") or {}
            expression = entry.get("expression") or license.get("id")
            if expression and "LicenseRef-" not in expression and (
                entry.get("expression") or expression in license_data["licenses"]
            ):
                expressions.append(expression)
                continue
            title = license.get("name") or expression
            if not title:
                raise FeedSbomError("Unrecognized license entry for %s" % package.name)
            attachment = license.get("text") or {}
            text = attachment.get("content")
            if text is None:
                # A name or URL is not the full license text required by SPDX.
                package.comment = (package.comment or "") + "\nCycloneDX declared license: " + json.dumps(
                    entry, sort_keys=True)
                objset.new_relationship(
                    [package], spdx.RelationshipType.hasDeclaredLicense,
                    [spdx.expandedlicensing_IndividualLicensingInfo.NoAssertionLicense])
                continue
            encoding = attachment.get("encoding")
            if encoding == "base64":
                try:
                    text = base64.b64decode(text, validate=True).decode("utf-8")
                except (ValueError, UnicodeDecodeError) as error:
                    raise FeedSbomError("Invalid license text for %s: %s" % (package.name, error)) from error
            elif encoding is not None:
                raise FeedSbomError("Unsupported license text encoding: %s" % encoding)
            licensing = objset.add(spdx.simplelicensing_SimpleLicensingText(
                _id=package._id + "/license/" + str(index),
                creationInfo=objset.doc.creationInfo,
                name=title,
                simplelicensing_licenseText=text,
            ))
            if license.get("url"):
                licensing.comment = license["url"]
            objset.new_relationship(
                [package], spdx.RelationshipType.hasDeclaredLicense, [licensing])
        if expressions:
            expression = expressions[0] if len(expressions) == 1 else " AND ".join(
                "(%s)" % term for term in expressions)
            licensing = objset.new_license_expression(expression, license_data)
            objset.new_relationship(
                [package], spdx.RelationshipType.hasDeclaredLicense, [licensing])

    def add_component(component, parent=None):
        nonlocal ordinal
        if not isinstance(component, dict) or not component.get("name"):
            raise FeedSbomError("Component under %s has no name" % (parent.name if parent else name))
        ordinal += 1
        package = objset.add(spdx.software_Package(
            _id=objset.new_spdxid("feed-package", name, version, arch, str(ordinal)),
            creationInfo=objset.doc.creationInfo,
            name=component["name"],
        ))
        if component.get("version"):
            package.software_packageVersion = component["version"]
        if component.get("purl"):
            package.software_packageUrl = component["purl"]
        if component.get("description"):
            package.description = component["description"]
        if component.get("copyright"):
            package.software_copyrightText = component["copyright"]
        if component.get("cpe"):
            package.externalIdentifier.append(spdx.ExternalIdentifier(
                externalIdentifierType=spdx.ExternalIdentifierType.cpe23,
                identifier=component["cpe"],
            ))
        supplier = component.get("supplier") or component.get("manufacturer") or {}
        supplier_name = supplier.get("name") or component.get("publisher")
        if supplier_name:
            agent = objset.find_filter(spdx.Organization, name=supplier_name)
            if agent is None:
                agent = objset.add(spdx.Organization(
                    _id=objset.new_spdxid("feed-supplier", supplier_name),
                    creationInfo=objset.doc.creationInfo, name=supplier_name,
                ))
            package.suppliedBy = agent
        for algorithm, value in sorted(_hashes(component).items()):
            algorithm = algorithm.lower()
            if algorithm not in spdx.HashAlgorithm.NAMED_INDIVIDUALS:
                raise FeedSbomError("Unsupported hash algorithm: %s" % algorithm)
            package.verifiedUsing.append(spdx.Hash(
                algorithm=getattr(spdx.HashAlgorithm, algorithm), hashValue=value))
        add_licenses(package, component.get("licenses"))
        ref = component.get("bom-ref")
        if ref:
            if ref in references:
                raise FeedSbomError("Duplicate CycloneDX bom-ref: %s" % ref)
            references[ref] = package
        if parent:
            objset.new_relationship([parent], spdx.RelationshipType.contains, [package])
        for child in component.get("components") or []:
            add_component(child, package)
        return package

    metadata = cdx.get("metadata") or {}
    main = dict(metadata["component"])
    if not main.get("licenses"):
        main["licenses"] = metadata.get("licenses")
    package = add_component(main)
    package.software_primaryPurpose = spdx.software_SoftwarePurpose.install
    package.software_sourceInfo = "Converted from CycloneDX %s; source: %s; serial: %s" % (
        cdx.get("specVersion"), source_url or "local SBOM cache", cdx.get("serialNumber", ""))
    if source_url and source_url.endswith(".cdx.sbom.json"):
        package.software_downloadLocation = source_url[:-len(".cdx.sbom.json")]
    for component in cdx.get("components") or []:
        add_component(component, package)
    for dependency in cdx.get("dependencies") or []:
        if dependency.get("provides"):
            raise FeedSbomError("CycloneDX dependency provides is not supported")
        refs = [dependency.get("ref"), *dependency.get("dependsOn", [])]
        if any(ref not in references for ref in refs):
            raise FeedSbomError("Unresolved CycloneDX dependency reference: %s" % refs)
        if len(refs) > 1:
            objset.new_relationship(
                [references[refs[0]]], spdx.RelationshipType.dependsOn,
                [references[ref] for ref in refs[1:]])
    return package
