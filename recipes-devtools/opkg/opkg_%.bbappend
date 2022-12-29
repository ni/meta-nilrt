FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://opkg.conf \
	file://opkg-signing.conf \
	file://gpg.conf \
	file://run-ptest \
	file://0001-opkg-key-add-keys-even-if-creation-date-is-in-the-fu.patch \
"

inherit ptest

PACKAGECONFIG = "libsolv gpg sha256 curl"

do_install:append () {
	install -d ${D}${sysconfdir}/opkg
	install -m 0644 ${WORKDIR}/opkg-signing.conf ${D}${sysconfdir}/opkg/
	install -d -m 0700 ${D}${sysconfdir}/opkg/gpg
	install -m 0644 ${WORKDIR}/gpg.conf ${D}${sysconfdir}/opkg/gpg/
}

RDEPENDS:${PN}-ptest += "bash"
