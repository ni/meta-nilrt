FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://consolegetty \
"

FILES_${PN} += "${sysconfdir}/init.d/consolegetty"

do_install_append() {
	install -d ${D}${sysconfdir}/init.d
	# script for calling getty only if console output is enabled 
	install -m 0755    ${WORKDIR}/consolegetty       ${D}${sysconfdir}/init.d
	# Remove OE getty lines for serial terminal and replace with consolegetty
	sed -i'' -e"s/\/sbin\/getty [0-9]\+ tty[OPS]\+[0-9]\+/\/etc\/init.d\/consolegetty tty2/g" ${D}${sysconfdir}/inittab
}
