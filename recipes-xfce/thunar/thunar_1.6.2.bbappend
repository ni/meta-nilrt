FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:"

SRC_URI =+ "file://thunar-wrapper"

do_install_append () {
	mv ${D}${bindir}/thunar ${D}${bindir}/thunar.bin
	install -m 0755 ${WORKDIR}/thunar-wrapper ${D}${bindir}/thunar
}