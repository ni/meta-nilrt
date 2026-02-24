FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://opkg.conf \
	file://opkg-signing.conf \
	file://gpg.conf \
	file://0001-opkg-gpg-only-enumerate-local-keys.patch \
	file://run-ptest \
"

inherit ptest

SRC_URI:append:armv7a = " \
	file://arm-kernel-arch.conf \
	file://test_arm_kernel_arch.sh \
"

PACKAGECONFIG = "libsolv gpg sha256 curl"

do_install:append () {
	install -d ${D}${sysconfdir}/opkg
	install -m 0644 ${UNPACKDIR}/opkg-signing.conf ${D}${sysconfdir}/opkg/
	install -d -m 0700 ${D}${sysconfdir}/opkg/gpg
	install -m 0644 ${UNPACKDIR}/gpg.conf ${D}${sysconfdir}/opkg/gpg/
}

RDEPENDS:${PN}-ptest += "bash"

do_install:append:armv7a () {
	install -d ${D}${sysconfdir}/opkg
	install -m 0644 ${UNPACKDIR}/arm-kernel-arch.conf ${D}${sysconfdir}/opkg/
}

do_install_ptest:append:armv7a () {
	install -m 0755 ${UNPACKDIR}/test_arm_kernel_arch.sh ${D}${PTEST_PATH}
}
