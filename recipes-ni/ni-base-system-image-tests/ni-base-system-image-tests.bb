SUMMARY = "Ptests for the base system image"
DESCRIPTION = "\
Installs ptests for the base system image."

SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

ALLOW_EMPTY:${PN} = "1"

SRC_URI = "\
    file://run-ptest \
    file://ptest-format.sh \
    file://fs_permissions_diff.py \
    file://test_fs_permissions_diff.sh \
"

S = "${WORKDIR}"

inherit ptest

do_install_ptest() {
    install -m 0644 ${WORKDIR}/ptest-format.sh        ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/run-ptest              ${D}${PTEST_PATH}
    install -m 0644 ${WORKDIR}/fs_permissions_diff.py ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_*.sh              ${D}${PTEST_PATH}
}

# We only want to build the -ptest package
PACKAGES:remove = "${PN}-dev ${PN}-staticdev ${PN}-dbg"

RDEPENDS:${PN}-ptest:append = " bash python3-pymongo"
