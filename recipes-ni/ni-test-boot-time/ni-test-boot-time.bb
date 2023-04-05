SUMMARY = "Ptest for measuring and logging the boot time"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

S = "${WORKDIR}"

ALLOW_EMPTY:${PN} = "1"

DEPENDS += "update-rc.d-native"
RDEPENDS:${PN}-ptest += "bash gawk python3 python3-pip python3-requests"
RDEPENDS:${PN}-ptest:append:x64 += "fw-printenv"
RDEPENDS:${PN}-ptest:append:armv7a += "u-boot-fw-utils"

SRC_URI = "\
	file://run-ptest \
	file://upload_results.py \
	file://zz-ni-record-boot-time \
"

do_install_ptest() {
	install -m 0755 ${S}/run-ptest		${D}${PTEST_PATH}
	install -m 0755 ${S}/upload_results.py	${D}${PTEST_PATH}

	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/zz-ni-record-boot-time	${D}${sysconfdir}/init.d
	update-rc.d -r ${D} zz-ni-record-boot-time	start 99 4 5 .
}

pkg_postinst_ontarget:${PN}-ptest:append() {
	python3 -m pip install influxdb
}

# We only want to build the -ptest package
PACKAGES:remove = "${PN}-dev ${PN}-staticdev ${PN}-dbg"
