require grub-nilrt.inc

GRUB_NILRT_IMAGE = "grubx64.efi"

PACKAGES =+ "${PN}-nilrt"
PROVIDES =+ "${PN}-nilrt"

do_install_append_class-target() {
    install -d ${D}/boot/

    grub-mkimage -p /boot/ -O ${GRUB_TARGET}-efi -d ./grub-core/ \
                 -o ${D}/boot/${GRUB_NILRT_IMAGE} ${GRUB_BUILDIN}

    install -m 0644 ${WORKDIR}/grub.cfg ${D}/boot/
}

FILES_${PN}-nilrt = "/boot/${GRUB_NILRT_IMAGE} \
                     /boot/grub.cfg"
