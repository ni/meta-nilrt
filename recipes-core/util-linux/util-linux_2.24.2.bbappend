FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

DEPENDS_class-target += "niacctbase"

RDEPENDS_util-linux-hwclock += "niacctbase"

SRC_URI =+ " file://removeSetUIDCheck.patch"

group = "${LVRT_GROUP}"

do_install_append() {
	chmod 4550 ${D}${base_sbindir}/hwclock
	chown 0:${group} ${D}${base_sbindir}/hwclock
}
