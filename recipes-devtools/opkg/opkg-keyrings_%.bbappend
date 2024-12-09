FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

python () {
    buildname = d.getVar('BUILDNAME')
    import re
    # Allow for BUILDNAME to be in the form of major.Minor or major.Minor.Update
    package_version = re.match(r'(\d+\.\d+\.\d+|\d+\.\d+)', buildname)
    if package_version:
        buildname = package_version.group(1)
    d.setVar('PKGV', buildname)
}

SRC_URI += " \
	file://nilrt-feed-2019.gpg \
	file://nilrt-feed-2023.gpg \
"

do_install:append() {
	# Install NI signing keys
	install -m 0444 ${WORKDIR}/nilrt-feed-2019.gpg ${D}${datadir}/opkg/keyrings/
	install -m 0444 ${WORKDIR}/nilrt-feed-2023.gpg ${D}${datadir}/opkg/keyrings/
}

PACKAGE_ADD_METADATA_IPK:opkg-keyrings = "DisplayName: Opkg-Keyrings\nUserVisible: yes\n"
