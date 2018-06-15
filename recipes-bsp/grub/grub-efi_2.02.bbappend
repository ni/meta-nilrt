FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

PV = "2.02+git${SRCPV}"
SRCREV = "${AUTOREV}"
GRUB_BRANCH = "nilrt/19.0"

SRC_URI = "\
        git://git.natinst.com/grub.git;branch=${GRUB_BRANCH} \
        file://grub.cfg \
        file://cfg \
"

S = "${WORKDIR}/git"

# Grub always try to force soft float in recent grub versions, even on x64, and this
# conflicts with how the x64 OE toolchain is set up. The only solution is to cache
# the empty variable to empty instead of no or yes, which will fail the build.
# More details: http://git.savannah.gnu.org/cgit/grub.git/tree/configure.ac?id=ce95549cc54b5d6f494608a7c390dba3aab4fba7#n790
CACHED_CONFIGUREVARS += "grub_cv_target_cc_soft_float="

GRUB_BUILDIN += "smbios chain multiboot efi_uga font gfxterm gfxmenu terminal \
                minicmd iorw echo reboot terminfo loopback memdisk tar help serial \
                ls search_fs_uuid udf btrfs ntfs reiserfs xfs lvm ata"

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
