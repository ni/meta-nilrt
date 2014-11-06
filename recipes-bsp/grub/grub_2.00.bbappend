DEPENDS += "niacctbase"

RDEPENDS_${PN}-grub-editenv += "niacctbase"

group = "${LVRT_GROUP}"

do_install_append() {
	chown 0:${group} ${D}${bindir}/grub-editenv
	chmod 0550 ${D}${bindir}/grub-editenv
}
