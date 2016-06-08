LIC_FILES_CHKSUM = "file://LICENSE;md5=fb92f464675f6b5df90f540d60237915"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "${NILRT_GIT}/salt.git;protocol=git;branch=nilrt/cardassia/develop \
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
"

SRCREV = "${AUTOREV}"
PV = "2016.3+git${SRCPV}"

S="${WORKDIR}/git"

PACKAGECONFIG = "tcp"

RDEPENDS_${PN}-minion += "python-pyinotify python-pyroute2"
RDEPENDS_${PN}-common_remove = "python-dateutil python-requests"

inherit update-rc.d

INITSCRIPT_PARAMS_${PN}-minion = "defaults 25 25"
