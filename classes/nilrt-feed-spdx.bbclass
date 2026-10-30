# Adds SPDX for NI packages that images install from package feeds.
#
# With BUILD_IMAGES_FROM_FEEDS, NI proprietary packages come prebuilt from the
# NI feeds and have no recipe, so create-spdx has no SPDX for them. Each NI
# package build publishes a CycloneDX SBOM next to its ipk in the NI package
# pool. For every installed package that has no SPDX provider, create-spdx-2.2
# calls SPDX_EXTERNAL_PACKAGE_FUNC; this class implements it by downloading
# the SBOM for the exact installed name, version and architecture, checking it
# matches the package, and converting it to an SPDX 2.2 document that is added
# to the image SPDX archive.
#
# The build fails if an installed package has neither a recipe nor a pool SBOM.
#
# Downloaded SBOMs are kept in NILRT_FEED_SBOM_CACHE. To build offline, put
# the SBOMs there (named <pkg>_<ver>_<arch>.ipk.cdx.sbom.json) and set
# BB_NO_NETWORK = "1".

NILRT_FEED_SBOM_POOL_URI ??= "http://argohttp.natinst.com/ni/linuxpkg/pool/ipk"
NILRT_FEED_SBOM_CACHE ??= "${DL_DIR}/nilrt-feed-sbom"
NILRT_FEED_SBOM_ORG ??= "NI"

SPDX_EXTERNAL_PACKAGE_FUNC = "nilrt_feed_spdx_package_doc"

def nilrt_feed_spdx_package_doc(d, name, info, outdir):
    import json
    from datetime import datetime, timezone
    from pathlib import Path
    import oe.spdx
    import oe.spdx_common
    from nilrt import feed_sbom

    version = info.get("ver")
    arch = info.get("arch")
    if not version or not arch:
        bb.fatal("nilrt-feed-spdx: opkg reported no version or architecture for %s" % name)

    try:
        sbom_path, source_url = feed_sbom.fetch_sbom(
            d.getVar("NILRT_FEED_SBOM_POOL_URI"),
            d.getVar("NILRT_FEED_SBOM_CACHE"),
            name, version, arch,
            allow_network=d.getVar("BB_NO_NETWORK") != "1")
    except feed_sbom.FeedSbomError as e:
        bb.fatal("nilrt-feed-spdx: %s has no SPDX from a recipe and no SBOM could be found: %s" % (name, e))

    with open(sbom_path) as f:
        cdx = json.load(f)

    lists_dir = d.expand("${IMAGE_ROOTFS}${OPKGLIBDIR}/opkg/lists")
    md5 = nilrt_feed_spdx_feed_md5(lists_dir).get((name, version, arch))
    problems = feed_sbom.verify_sbom(cdx, name, version, arch, md5)
    if problems:
        bb.fatal("nilrt-feed-spdx: SBOM %s does not match installed package %s %s (%s):\n  %s" % (
            sbom_path, name, version, arch, "\n  ".join(problems)))
    if not md5:
        bb.note("nilrt-feed-spdx: no feed MD5 for %s %s (%s); SBOM checked by name, version and architecture only" % (
            name, version, arch))

    license_data = oe.spdx_common.load_spdx_license_data(d)
    namespace = feed_sbom.document_namespace(
        d.getVar("SPDX_NAMESPACE_PREFIX"), d.getVar("SPDX_UUID_NAMESPACE"), name, version, arch)
    doc = feed_sbom.cyclonedx_to_spdx(
        oe.spdx, cdx, name, version, arch, namespace,
        datetime.now(tz=timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        source_url=source_url,
        license_ids=license_data["licenses"].keys(),
        license_list_version=get_license_list_version(license_data, d),
        organization=d.getVar("NILRT_FEED_SBOM_ORG"))

    out = Path(outdir) / ("%s.spdx.json" % doc.name)
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("wb") as f:
        doc.to_json(f, sort_keys=True, indent=get_json_indent(d))
    bb.note("nilrt-feed-spdx: %s %s (%s) <- %s" % (name, version, arch, source_url or sbom_path))
    return out

def nilrt_feed_spdx_feed_md5(lists_dir):
    from nilrt import feed_sbom
    cache = nilrt_feed_spdx_feed_md5.__dict__.setdefault("cache", {})
    if lists_dir not in cache:
        cache[lists_dir] = feed_sbom.parse_opkg_lists(lists_dir)
    return cache[lists_dir]

nilrt_feed_spdx_package_doc[vardepsexclude] += "BB_NO_NETWORK NILRT_FEED_SBOM_CACHE"
