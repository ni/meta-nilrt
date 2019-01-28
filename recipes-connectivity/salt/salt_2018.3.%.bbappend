LIC_FILES_CHKSUM = "file://LICENSE;md5=c996f5a78d858a52c894fa3f4bec68c1"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "git://github.com/ni/salt.git;branch=ni/master/2018.3 \
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
    python3-avahi \
    python3-mmap \
    python3-pyinotify \
    python3-pyroute2 \
    python3-pika \
    python3-psutil \
    ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'python3-pyconnman', '', d)} \
"

RDEPENDS_${PN}-minion_append_armv7a += "\
    ${@oe.utils.conditional('DISTRO', 'nilrt', 'u-boot-mkimage', '', d)} \
"

# these dependencies should NOT be added to the salt bb recipe as they're added
# here in the bbappend for the NIFeeds ni-skyline packages. In the future these
# will be removed and skyline packages made to depend on them directly.
RDEPENDS_${PN}-common_append += " \
    python3-configparser \
    python3-dateutil \
    python3-difflib \
    python3-distutils \
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
