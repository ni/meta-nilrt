FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

# Pull from master branch, rather than a release tarball
SRC_URI = "git://git.yoctoproject.org/opkg.git \
           file://opkg-configure.service \
           file://0001-opkg_conf-create-opkg.lock-in-run-instead-of-var-run.patch \
"

SRCREV ?= "${AUTOREV}"
PV= "0.3.5-pre+${SRCPV}"

S = "${WORKDIR}/git"

SRC_URI += " \
            file://opkg.conf \
            file://test_feedserver.sh \
            file://test_ni_pxi_install.sh \
            file://run-ptest \
           "

PACKAGECONFIG = "libsolv"

RDEPENDS_${PN}-ptest += "bash"

do_install_ptest() {
        cp ${WORKDIR}/test_feedserver.sh ${D}${PTEST_PATH}
        cp ${WORKDIR}/test_ni_pxi_install.sh ${D}${PTEST_PATH}
}
