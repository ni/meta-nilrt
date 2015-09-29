LIC_FILES_CHKSUM = "file://LICENSE;md5=b59c9134761722281bb895f65cb15e9a"

SRC_URI = "git://git.amer.corp.natinst.com/salt.git;protocol=git;branch=nilrt/comms-2.0/2015.8 \
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
PV = "2015.8+git${SRCPV}"

S="${WORKDIR}/git"

RDEPENDS_${PN}-minion += "python-pyinotify python-pyroute2"
RDEPENDS_${PN}-common_remove = "python-dateutil python-requests"

inherit update-rc.d

INITSCRIPT_PARAMS_${PN}-minion = "defaults 25 25"
