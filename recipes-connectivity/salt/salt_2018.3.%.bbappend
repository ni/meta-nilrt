LIC_FILES_CHKSUM = "file://LICENSE;md5=fb92f464675f6b5df90f540d60237915"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "git://github.com/ni/salt.git;branch=ni/master/2018.3 \
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
PV = "2018.3+git${SRCPV}"

S="${WORKDIR}/git"

PACKAGECONFIG = "tcp"

RDEPENDS_${PN}-minion_append += "\
    python3-avahi \
    python3-iso8601 \
    python3-pyinotify \
    python3-pyroute2 \
    python3-pika \
    python3-psutil \
    ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'python3-pyconnman', '', d)} \
"

RDEPENDS_${PN}-minion_append_armv7a += "\
    ${@oe.utils.conditional('DISTRO', 'nilrt', 'u-boot-mkimage', '', d)} \
"

RDEPENDS_${PN}-common_append += " \
    python3-difflib \
    python3-distutils \
    python3-configparser \
    python3-misc \
    python3-multiprocessing \
    python3-profile \
    python3-pyiface \
    python3-resource \
    python3-terminal \
    python3-unixadmin \
    python3-xmlrpc \
"

# Note that the salt test suite (salt-tests) require python-pyzmq to run
# properly even though we run them in tcp mode
RDEPENDS_${PN}-tests_append += "\
    python3-pyzmq \
    python3-six \
"

RDEPENDS_${PN}-ptest += "salt-tests"

inherit update-rc.d ptest
INITSCRIPT_PARAMS_${PN}-minion = "defaults 93 7"

do_install_ptest_append() {
    install -m 0755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
}
