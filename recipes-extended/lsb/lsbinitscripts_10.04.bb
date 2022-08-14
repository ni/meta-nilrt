SUMMARY = "SysV init scripts which are only used in an LSB image"
HOMEPAGE = "https://wiki.debian.org/LSBInitScripts"
SECTION = "base"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"


DEPENDS = "popt glib-2.0"

SRC_URI = "git://github.com/fedora-sysv/initscripts;protocol=https"
SRCREV = "d2243a0912bbad57b1b413f2c15599341cb2aa76"
UPSTREAM_CHECK_GITTAGREGEX = "^(?P<pver>\d+(\.\d+)+)"


S = "${WORKDIR}/git"

# Since we are only taking the functions file directly, no need to
# configure or compile anything so do not execute these
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install(){
	install -d ${D}${sysconfdir}/init.d/
	install -m 0644 ${S}/etc/rc.d/init.d/functions ${D}${sysconfdir}/init.d/functions
}


RDEPENDS:${PN} += "util-linux"
RPROVIDES:${PN} += "initd-functions"
RCONFLICTS:${PN} = "initscripts-functions"
