LIC_FILES_CHKSUM = "file://LICENSE;md5=fb92f464675f6b5df90f540d60237915"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "git://github.com/ni/salt.git;branch=ni/skyline-18.2/2018.3 \
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
PV = "2018.3+git${SRCPV}"

S="${WORKDIR}/git"

PACKAGECONFIG = "tcp"

RDEPENDS_${PN}-minion_append += "\
    python-avahi \
    python-pyinotify \
    python-pyroute2 \
    python-pika \
    python-argparse \
    python-psutil \
    ${@base_conditional('DISTRO', 'nilrt-nxg', 'python-pyconnman', '', d)} \
"

RDEPENDS_${PN}-minion_append_armv7a += "\
    ${@base_conditional('DISTRO', 'nilrt', 'u-boot-mkimage', '', d)} \
"

# these dependencies should NOT be added to the salt bb recipe as they're added
# here in the bbappend for the NIFeeds ni-skyline packages. In the future these
# will be removed and skyline packages made to depend on them directly.
RDEPENDS_${PN}-common_append += " \
    python-dateutil \
    python-difflib \
    python-distutils \
    python-enum34 \
    python-importlib \
    python-misc \
    python-multiprocessing \
    python-profile \
    python-resource \
    python-terminal \
    python-unixadmin \
    python-xmlrpc \
"

# Note that the salt test suite (salt-tests) require python-pyzmq to run
# properly even though we run them in tcp mode
RDEPENDS_${PN}-tests_append += "\
    python-pyzmq \
    python-six \
"

RDEPENDS_${PN}-ptest += "salt-tests"

inherit update-rc.d ptest
INITSCRIPT_PARAMS_${PN}-minion = "defaults 93 7"

do_install_ptest_append() {
    install -m 0755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
}
