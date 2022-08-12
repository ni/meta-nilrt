FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
	file://opkg.conf \
	file://opkg-signing.conf \
	file://gpg.conf \
	file://run-ptest \
"

PACKAGECONFIG = "libsolv gpg sha256 curl"

RDEPENDS:${PN}-ptest += "bash"

do_install:append () {
	install -d ${D}${sysconfdir}/opkg
	install -m 0644 ${WORKDIR}/opkg-signing.conf ${D}${sysconfdir}/opkg/
	install -d -m 0700 ${D}${sysconfdir}/opkg/gpg
	install -m 0644 ${WORKDIR}/gpg.conf ${D}${sysconfdir}/opkg/gpg/
}
