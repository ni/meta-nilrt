# Include the inventory published for prebuilt feed packages in image SPDX 3.0.

NILRT_FEED_SBOM_POOL_URI ??= "http://argohttp.natinst.com/ni/linuxpkg/pool/ipk"
NILRT_FEED_SBOM_CACHE ??= "${DL_DIR}/nilrt-feed-sbom"
NILRT_FEED_SBOM_MD5 = "${SPDXDIR}/feed-package-md5.json"

SPDX_EXTERNAL_PACKAGE_FUNC = "${@'nilrt_feed_spdx_package' if d.getVar('BUILD_IMAGES_FROM_FEEDS') == '1' and bb.data.inherits_class('image', d) else ''}"

ROOTFS_POSTUNINSTALL_COMMAND:append = " nilrt_feed_spdx_collect_checksums;"
do_rootfs[file-checksums] += "${NILRT_FEED_SBOM_LIB}:True"
do_create_rootfs_spdx[file-checksums] += "${NILRT_FEED_SBOM_LIB}:True"
do_create_rootfs_spdx[network] = "1"

python nilrt_feed_spdx_collect_checksums() {
    import json
    from pathlib import Path
    from nilrt import feed_sbom

    lists_dir = d.expand("${IMAGE_ROOTFS}${OPKGLIBDIR}/opkg/lists")
    try:
        checksums = feed_sbom.parse_opkg_lists(lists_dir)
    except feed_sbom.FeedSbomError as error:
        bb.fatal("nilrt-feed-spdx: %s" % error)
    path = Path(d.getVar("NILRT_FEED_SBOM_MD5"))
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w") as stream:
        json.dump([[*identity, md5] for identity, md5 in sorted(checksums.items())], stream)
}

def nilrt_feed_spdx_package(d, objset, name, info):
    import json
    from pathlib import Path
    import bb.fetch2
    import oe.spdx_common
    from nilrt import feed_sbom

    version, arch = info.get("ver"), info.get("arch")
    if not version or not arch:
        bb.fatal("nilrt-feed-spdx: missing installed version/architecture for %s" % name)

    checksums = json.loads(Path(d.getVar("NILRT_FEED_SBOM_MD5")).read_text())
    md5 = next((value for pkg, ver, architecture, value in checksums
                if (pkg, ver, architecture) == (name, version, arch)), None)
    if not md5:
        bb.fatal("nilrt-feed-spdx: no feed IPK MD5 for %s %s (%s)" % (name, version, arch))

    def get(url):
        bb.fetch2.check_network_access(d, "nilrt-feed-spdx", url)
        if url.startswith("http:"):
            bb.warnonce("nilrt-feed-spdx: the SBOM pool uses unauthenticated HTTP; "
                        "IPK MD5 checks select the build but do not authenticate SBOM inventory")
        return feed_sbom.http_get(url)

    try:
        bb.note("nilrt-feed-spdx: locating SBOM for %s %s (%s)" % (name, version, arch))
        cache = Path(d.getVar("NILRT_FEED_SBOM_CACHE"))
        cache.mkdir(parents=True, exist_ok=True)
        lock = cache / (feed_sbom.sbom_filename(name, version, arch) + ".lock")
        with bb.utils.fileslocked([str(lock)]):
            path, source = feed_sbom.fetch_sbom(
                d.getVar("NILRT_FEED_SBOM_POOL_URI"), str(cache),
                name, version, arch, allow_network=d.getVar("BB_NO_NETWORK") != "1",
                get=get, md5=md5)
            cdx = json.loads(Path(path).read_text())
            problems = feed_sbom.verify_sbom(cdx, name, version, arch, md5)
            if problems:
                raise feed_sbom.FeedSbomError("; ".join(problems))
        package = feed_sbom.cyclonedx_to_spdx30(
            objset, cdx, name, version, arch,
            oe.spdx_common.load_spdx_license_data(d), source_url=source)
    except feed_sbom.FeedSbomError as error:
        bb.fatal("nilrt-feed-spdx: %s: %s" % (name, error))
    bb.note("nilrt-feed-spdx: included %s %s (%s) from %s" % (
        name, version, arch, source or path))
    return package

nilrt_feed_spdx_package[vardepsexclude] += "BB_NO_NETWORK NILRT_FEED_SBOM_CACHE"
