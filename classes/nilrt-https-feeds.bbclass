# Local build inputs may use file://; all network feeds must use HTTPS.

python do_validate_https_feeds() {
    from urllib.parse import urlsplit

    feeds = [
        ("NILRT_FEEDS_URI", d.getVar("NILRT_FEEDS_URI")),
        ("NILRT_MACHINE_FEED_URI_nimain", d.getVar("NILRT_MACHINE_FEED_URI_nimain")),
    ]
    for uri in (d.getVar("PACKAGE_FEED_URIS") or "").split():
        feeds.append(("PACKAGE_FEED_URIS", uri))
    for entry in (d.getVar("IPK_FEED_URIS") or "").split():
        name, separator, uri = entry.partition("##")
        if not separator or not name or not uri:
            bb.fatal("%s: invalid IPK_FEED_URIS entry %r" % (d.getVar("PN"), entry))
        if not uri.startswith("file://"):
            feeds.append(("IPK_FEED_URIS[%s]" % name, uri))
    for name in (d.getVarFlags("PACKAGE_FEED_URIS_ADDITIONAL") or {}):
        if name != "doc":
            feeds.append((
                "PACKAGE_FEED_URIS_ADDITIONAL[%s]" % name,
                d.getVarFlag("PACKAGE_FEED_URIS_ADDITIONAL", name, expand=True),
            ))

    for source, uri in feeds:
        try:
            parsed = urlsplit(uri or "")
            valid = parsed.scheme == "https" and parsed.hostname and not any(
                char.isspace() for char in (uri or "")
            )
        except ValueError:
            valid = False
        if not valid:
            bb.fatal("%s: %s must be an HTTPS feed URI, got %r" % (
                d.getVar("PN"), source, uri))
}

do_validate_https_feeds[vardeps] += "PACKAGE_FEED_URIS_ADDITIONAL"
addtask validate_https_feeds before do_rootfs

python nilrt_validate_https_feed_confs() {
    from pathlib import Path
    from urllib.parse import urlsplit

    conf_dir = Path(d.getVar("IMAGE_ROOTFS")) / "etc" / "opkg"
    for path in sorted(conf_dir.glob("*.conf")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            fields = line.split()
            if len(fields) >= 2 and fields[0] == "option" and fields[1] in (
                "ssl_dont_verify_peer", "follow_location",
            ):
                bb.fatal("%s:%d: unsafe target opkg option: %s" % (
                    path, number, fields[1]))
            if not fields or fields[0] not in ("src", "src/gz"):
                continue
            valid = False
            if len(fields) == 3:
                try:
                    parsed = urlsplit(fields[2])
                    valid = parsed.scheme == "https" and parsed.hostname
                except ValueError:
                    pass
            if not valid:
                bb.fatal("%s:%d: target opkg feed must use HTTPS: %s" % (
                    path, number, line))
}

do_rootfs[postfuncs] += "nilrt_validate_https_feed_confs"
