FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://nilrt-feed-2019.gpg \
	file://nilrt-feed-2023.gpg \
"

do_install:append() {
	# Install NI signing keys
	install -m 0444 ${WORKDIR}/nilrt-feed-2019.gpg ${D}${datadir}/opkg/keyrings/
	install -m 0444 ${WORKDIR}/nilrt-feed-2023.gpg ${D}${datadir}/opkg/keyrings/
}
