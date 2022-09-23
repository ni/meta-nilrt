SUMMARY = "nilrt specific modification for kdump"
FILESEXTRAPATHS:prepend := "${THISDIR}/kexec-tools:"

SRC_URI += " file://kdump.conf \
           "

pkg_postinst:kdump() {
    sed -i 's/set otherbootargs="\(.*\)"/set otherbootargs="\1 crashkernel=128M "/' /boot/runmode/bootimage.cfg
}
