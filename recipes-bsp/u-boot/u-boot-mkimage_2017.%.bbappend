do_install_append () {
	install -d ${D}${bindir}
	install -m 0755 tools/dumpimage ${D}${bindir}/uboot-dumpimage
	ln -sf uboot-dumpimage ${D}${bindir}/dumpimage
}
