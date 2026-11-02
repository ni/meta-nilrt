#
# Copyright (c) 2026 Emerson Electric Co.
#
# SPDX-License-Identifier: MIT
#
"""Fetch the CycloneDX SBOM published for a prebuilt NI feed package and
convert it to an SPDX 2.2 document that create-spdx-2.2 can combine into the
image SBOM.

NI packages installed from feeds have no recipe in the build, so OE-core
cannot generate SPDX for them. Each package's build publishes
<pkg>_<ver>_<arch>.ipk.cdx.sbom.json next to the ipk in the NI package pool:

    <pool>/<prefix>/<pkg>/<X.Y.Z>/<ver>/<build-hash>/<pkg>_<ver>_<arch>.ipk.cdx.sbom.json

This module has no BitBake dependency so it can be unit tested on its own.
"""

import html.parser
import json
import os
import re
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

SBOM_SUFFIX = ".ipk.cdx.sbom.json"

SPDX_TOOL = "Tool: meta-nilrt nilrt-feed-spdx.bbclass"

_ID_INVALID = re.compile(r"[^A-Za-z0-9.-]+")


class FeedSbomError(Exception):
    pass


def spdx_id_part(text):
    """Return text restricted to the characters SPDX allows in identifiers."""
    return _ID_INVALID.sub("-", text).strip("-.") or "unknown"


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
                        result[key] = fields["MD5Sum"].lower()
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


def document_namespace(prefix, uuid_namespace, name, version, arch):
    ns_uuid = uuid.uuid5(uuid.NAMESPACE_DNS, uuid_namespace)
    key = "%s_%s_%s" % (name, version, arch)
    return "%s/%s-%s" % (prefix.rstrip("/"), spdx_id_part(name), uuid.uuid5(ns_uuid, key))


class _Licenses:
    """Map CycloneDX licenses to SPDX expressions, collecting LicenseRefs."""

    def __init__(self, spdx, known_ids):
        self.spdx = spdx
        self.known = set(known_ids or ())
        self.extracted = {}

    def _ref(self, name, url=None):
        ident = "LicenseRef-" + spdx_id_part(name)
        if ident not in self.extracted:
            info = self.spdx.SPDXExtractedLicensingInfo()
            info.licenseId = ident
            info.name = name
            info.extractedText = name + ("\n" + url if url else "")
            if url:
                info.comment = url
            self.extracted[ident] = info
        return ident

    _OPERATORS = {"AND", "OR", "WITH"}

    def _normalize_expression(self, expr):
        """Make every license id in expr valid for SPDX 2.2.

        Custom LicenseRef-* ids get extracted licensing info; ids that are not
        on the SPDX license list become LicenseRefs.
        """
        tokens = re.split(r"([A-Za-z0-9.+:-]+)", expr)
        after_with = False
        for i in range(1, len(tokens), 2):
            token = tokens[i]
            if token in self._OPERATORS:
                after_with = token == "WITH"
                continue
            if after_with or ":" in token:
                # exception ids and DocumentRef-*:LicenseRef-* stay unchanged
                pass
            elif token.startswith("LicenseRef-"):
                tokens[i] = self._ref(token[len("LicenseRef-"):])
            elif self.known and token.rstrip("+") not in self.known:
                tokens[i] = self._ref(token)
            after_with = False
        return "".join(tokens)

    def expression(self, licenses):
        terms = []
        for entry in licenses or []:
            if entry.get("expression"):
                terms.append("(%s)" % self._normalize_expression(entry["expression"]))
                continue
            lic = entry.get("license") or {}
            if lic.get("id") and (not self.known or lic["id"] in self.known):
                terms.append(lic["id"])
            elif lic.get("id") or lic.get("name"):
                terms.append(self._ref(lic.get("name") or lic["id"], lic.get("url")))
        terms = list(dict.fromkeys(terms))
        if not terms:
            return "NOASSERTION"
        if len(terms) == 1 and terms[0].startswith("("):
            return terms[0][1:-1]
        return " AND ".join(terms)


def _supplier(component, default=None):
    for key in ("supplier", "manufacturer"):
        name = (component.get(key) or {}).get("name")
        if name:
            return "Organization: %s" % name
    if component.get("publisher"):
        return "Organization: %s" % component["publisher"]
    return default or "NOASSERTION"


def _fill_package(spdx, pkg, component, licenses):
    pkg.versionInfo = component.get("version") or "NOASSERTION"
    pkg.licenseDeclared = licenses.expression(component.get("licenses"))
    if component.get("copyright"):
        pkg.copyrightText = component["copyright"]
    if component.get("description"):
        pkg.description = component["description"]
    for alg, value in sorted(_hashes(component).items()):
        if alg in ("MD5", "SHA1", "SHA256", "SHA384", "SHA512"):
            c = spdx.SPDXChecksum()
            c.algorithm = alg
            c.checksumValue = value
            pkg.checksums.append(c)
    if component.get("purl"):
        ref = spdx.SPDXExternalReference()
        ref.referenceCategory = "PACKAGE-MANAGER"
        ref.referenceType = "purl"
        ref.referenceLocator = component["purl"]
        pkg.externalRefs.append(ref)
    if component.get("cpe"):
        ref = spdx.SPDXExternalReference()
        ref.referenceCategory = "SECURITY"
        ref.referenceType = "cpe23Type"
        ref.referenceLocator = component["cpe"]
        pkg.externalRefs.append(ref)


def cyclonedx_to_spdx(spdx, cdx, name, version, arch, namespace, created,
                      source_url=None, license_ids=None, license_list_version=None,
                      organization="NI"):
    """Return an oe.spdx.SPDXDocument for one feed package.

    The document describes one package whose name is the opkg package name,
    so create-spdx can match it to the installed package. The NI and
    third-party components in both CycloneDX component trees are added as
    packages, preserving each parent's CONTAINS relationships.

    spdx is the oe.spdx module, passed in so this module has no OE dependency.
    """
    licenses = _Licenses(spdx, license_ids)
    meta = cdx.get("metadata") or {}
    main = meta.get("component") or {}

    doc = spdx.SPDXDocument()
    doc.name = spdx_id_part(name)
    doc.documentNamespace = namespace
    doc.creationInfo.created = created
    if license_list_version:
        doc.creationInfo.licenseListVersion = license_list_version
    doc.creationInfo.creators.append(SPDX_TOOL)
    doc.creationInfo.creators.append("Organization: %s" % organization)
    doc.creationInfo.comment = (
        "Converted from the CycloneDX SBOM published for the NI feed package %s %s (%s)%s%s." % (
            name, version, arch,
            " at " + source_url if source_url else "",
            ", serialNumber " + cdx["serialNumber"] if cdx.get("serialNumber") else ""))

    pkg = spdx.SPDXPackage()
    pkg.name = name
    pkg.SPDXID = "SPDXRef-Package-%s" % spdx_id_part(name)
    pkg.supplier = _supplier(main, "Organization: %s" % organization)
    pkg.packageFileName = "%s_%s_%s.ipk" % (name, version, arch)
    if source_url and source_url.endswith(".cdx.sbom.json"):
        pkg.downloadLocation = source_url[:-len(".cdx.sbom.json")]
    _fill_package(spdx, pkg, main, licenses)
    pkg.versionInfo = version
    if pkg.licenseDeclared == "NOASSERTION":
        pkg.licenseDeclared = licenses.expression(meta.get("licenses"))
    doc.packages.append(pkg)
    doc.add_relationship(doc, "DESCRIBES", pkg)

    def add_components(parent, components):
        for component in components or []:
            if not component.get("name"):
                raise FeedSbomError("Component under %s has no name" % parent.name)
            child = spdx.SPDXPackage()
            child.name = component["name"]
            child.SPDXID = "SPDXRef-Component-%d-%s" % (
                len(doc.packages), spdx_id_part(component["name"]))
            child.supplier = _supplier(component)
            _fill_package(spdx, child, component, licenses)
            doc.packages.append(child)
            doc.add_relationship(parent, "CONTAINS", child)
            add_components(child, component.get("components"))

    add_components(pkg, cdx.get("components"))
    add_components(pkg, main.get("components"))

    doc.hasExtractedLicensingInfos = list(licenses.extracted.values())
    return doc
