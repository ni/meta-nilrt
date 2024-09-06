SUMMARY = "SysV init scripts which are only used in an LSB image"
HOMEPAGE = "https://wiki.debian.org/LSBInitScripts"
SECTION = "base"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"


DEPENDS = "popt glib-2.0"

SRC_URI = "git://github.com/fedora-sysv/initscripts;protocol=https;branch=main"
SRCREV = "79ec53026623b76d890b6dcdebf30d0c52d11a8b"
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
