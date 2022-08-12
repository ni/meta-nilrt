FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI += " \
	file://opkg.conf \
	file://opkg-signing.conf \
	file://gpg.conf \
	file://run-ptest \
"

SRC_URI:append:armv7a = " \
	file://arm-kernel-arch.conf \
	file://test_arm_kernel_arch.sh \
"

PACKAGECONFIG = "libsolv gpg sha256 curl"

RDEPENDS:${PN}-ptest += "bash"

do_install:append () {
	install -d ${D}${sysconfdir}/opkg
	install -m 0644 ${WORKDIR}/opkg-signing.conf ${D}${sysconfdir}/opkg/
	install -d -m 0700 ${D}${sysconfdir}/opkg/gpg
	install -m 0644 ${WORKDIR}/gpg.conf ${D}${sysconfdir}/opkg/gpg/
}

do_install:append:armv7a () {
	install -d ${D}${sysconfdir}/opkg
	install -m 0644 ${WORKDIR}/arm-kernel-arch.conf ${D}${sysconfdir}/opkg/
}

do_install_ptest:append:armv7a () {
	install -m 0755 ${WORKDIR}/test_arm_kernel_arch.sh ${D}${PTEST_PATH}
}
