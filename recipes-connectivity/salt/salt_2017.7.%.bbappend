LIC_FILES_CHKSUM = "file://LICENSE;md5=fb92f464675f6b5df90f540d60237915"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "${NILRT_GIT}/salt.git;protocol=git;branch=ni/skyline-18.1/2017.7 \
           file://set_python_location_hashbang.patch \
           file://minion \
           file://salt-minion \
           file://salt-common.bash_completion \
           file://salt-common.logrotate \
           file://salt-api \
           file://salt-master \
           file://master \
           file://salt-syndic \
           file://cloud \
           file://roster \
           file://run-ptest \
"

SRCREV = "${AUTOREV}"
PV = "2017.7+git${SRCPV}"

S="${WORKDIR}/git"

PACKAGECONFIG = "tcp"

RDEPENDS_${PN}-minion = "python ${PN}-common (= ${EXTENDPKGV}) python-msgpack python-avahi python-pyinotify python-pyroute2 python-pycrypto python-pika python-argparse python-psutil ${@base_conditional('DISTRO', 'nilrt-nxg', 'python-pyconnman', '', d)}"
RDEPENDS_${PN}-minion_append_armv7a += "${@base_conditional('DISTRO', 'nilrt', 'u-boot-mkimage', '', d)}"
RDEPENDS_${PN}-common = " \
    lsb \
    python \
    python-difflib \
    python-distutils \
    python-futures \
    python-importlib \
    python-jinja2 \
    python-misc \
    python-multiprocessing \
    python-profile \
    python-pyyaml \
    python-resource \
    python-terminal \
    python-tornado (>= 4.2.1) \
    python-unixadmin \
    python-xmlrpc \
    "
RDEPENDS_${PN}-ssh = "python ${PN}-common (= ${EXTENDPKGV}) python-msgpack"
RDEPENDS_${PN}-api = "python ${PN}-master"
RDEPENDS_${PN}-master = "python ${PN}-common (= ${EXTENDPKGV}) python-msgpack python-pycrypto"
RDEPENDS_${PN}-syndic = "python ${PN}-master (= ${EXTENDPKGV})"
RDEPENDS_${PN}-cloud = "python ${PN}-common (= ${EXTENDPKGV})"
RDEPENDS_${PN}-tests += "python-pyzmq python-six python-image"
RDEPENDS_${PN}-ptest += "salt-tests"
# Note that the salt test suite (salt-tests) require python-pyzmq to run
# properly even though we run them in tcp mode

inherit update-rc.d ptest
INITSCRIPT_PARAMS_${PN}-minion = "defaults 93 7"

do_install_ptest_append() {
    install -m 0755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
}
