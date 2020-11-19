SUMMARY = "Ptests for the nirtcfg utility"
DESCRIPTION = "\
Installs ptests for the nirtcfg utilty."

SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "\
    file://configs/ \
    file://run-ptest \
    file://section_chars \
    file://setup.sh \
    file://shared-functions.sh \
    file://teardown.sh \
    file://test_binary.sh \
    file://test_clear.sh \
    file://test_get.sh \
    file://test_list.sh \
    file://test_rm-if-empty.sh \
    file://test_set.sh \
"

S = "${WORKDIR}"

inherit ptest

do_install_ptest() {
    install -m 0755 -d ${D}${PTEST_PATH}/configs
    install -m 0755 ${WORKDIR}/configs/*           ${D}${PTEST_PATH}/configs
    install -m 0644 ${WORKDIR}/ptest-format.sh     ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/run-ptest           ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/section_chars       ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/setup.sh            ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/shared-functions.sh ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/teardown.sh         ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_*.sh           ${D}${PTEST_PATH}
}

# We only want to build the -ptest package
PACKAGES_remove = "${PN}-dev ${PN}-staticdev ${PN}-dbg"

RDEPENDS_${PN}-ptest_append = "bash"
