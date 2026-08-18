SUMMARY = "Ptests confirming ni-auth and niacctbase-sudo are not installed"
DESCRIPTION = "\
Installs ptests that verify ni-auth and niacctbase-sudo packages are not \
present on the system, as required by the SNAC configuration."

SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "\
    file://run-ptest \
    file://ptest-format.sh \
    file://test_niauth_packages.sh \
"

S = "${UNPACKDIR}"

inherit ptest

do_install_ptest() {
    install -m 0755 ${S}/run-ptest              ${D}${PTEST_PATH}
    install -m 0644 ${S}/ptest-format.sh        ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_niauth_packages.sh ${D}${PTEST_PATH}
}

ALLOW_EMPTY:${PN} = "1"

# Only build the base (empty) and -ptest packages
PACKAGES:remove = "${PN}-dev ${PN}-staticdev ${PN}-dbg"

RDEPENDS:${PN}-ptest:append = " bash opkg"
