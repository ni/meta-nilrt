SUMMARY = "nilrt specific modification for kdump"
FILESEXTRAPATHS_prepend := "${THISDIR}/kexec-tools:"

SRC_URI += " file://kdump.conf \
           "

pkg_postinst_kdump() {
    sed -i 's/set otherbootargs="\(.*\)"/set otherbootargs="\1 crashkernel=128M "/' /boot/runmode/bootimage.cfg
}
