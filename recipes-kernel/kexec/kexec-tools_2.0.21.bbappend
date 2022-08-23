SUMMARY = "nilrt specific modification for kdump"


SRC_URI += " \
	file://kdump.conf \
"


FILESEXTRAPATHS:prepend := "${THISDIR}/kexec-tools:"


pkg_postinst_kdump() {
	sed -i 's/set otherbootargs="\(.*\)"/set otherbootargs="\1 crashkernel=128M "/' /boot/runmode/bootimage.cfg
}
