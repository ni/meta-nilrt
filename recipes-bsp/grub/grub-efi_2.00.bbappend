# This .bbappend file changes the origin of source files and
# uses git instead of ftp.

FILESEXTRAPATHS_prepend := "${THISDIR}/grub-efi:"

SRCREV = "9585647a25b65f98db6bd22c569b34795512f046"
# Uses the same commit and the same patches which are used by grub in recoverytool
# (/nilinux/bootloader/grub2)

SRC_URI = "git://git.savannah.gnu.org/grub.git \
           file://grub-2.00-fpmath-sse-387-fix.patch \
           file://autogen.sh-exclude-pc.patch \
           file://0001-Fix-build-with-glibc-2.20.patch \
           file://grub-2.00-add-oe-kernel.patch \
           file://0001-Define-SMBIOS3-entry-point-structures-for-EFI.patch \
           file://0002-Add-a-module-for-retrieving-SMBIOS-information.patch \
           file://cfg \
           file://add_inbit_comm.patch \
	   file://grub-efi-cleanup-menu.patch \
           file://grub.cfg \
           "

S = "${WORKDIR}/git"

PACKAGES =+ "grub-efi-nilrt"

PROVIDES =+ " grub-efi-nilrt "

GRUB_NILRT_IMAGE = "grubx64.efi"

SUMMARY_grub-efi-nilrt = "Grub efi standalone image for NI Linux RT"
DESCRIPTION_grub-efi-nilrt = "GRUB image used to load the NI Linux RT for x86_64 \
machines. This image is contained into one single ${GRUB_NILRT_IMAGE} file placed under \
/boot/ folder. Package also contains grub.cfg which controls the boot logic and \
is placed under the same path."

FILES_grub-efi-nilrt = "/boot/${GRUB_NILRT_IMAGE} \
                        /boot/grub.cfg "

GRUB_BUILDIN = "boot linux ext2 fat serial part_msdos part_gpt normal efi_gop search \
                chain configfile multiboot efi_uga font gfxterm gfxmenu terminal \
                minicmd test iorw loadenv echo reboot terminfo loopback memdisk tar help \
                ls search_fs_uuid udf btrfs reiserfs xfs lvm ata smbios "

do_compile_append_class-target () {
    grub-mkimage -p /boot/ -O ${GRUB_TARGET}-efi -d ./grub-core/ \
                 -o ${B}/${GRUB_NILRT_IMAGE} ${GRUB_BUILDIN}
}

do_install_append_class-target () {
    install -d ${D}/boot/
    install -m 0644 ${B}/${GRUB_NILRT_IMAGE} ${D}/boot/
    install -m 0644 ${WORKDIR}/grub.cfg ${D}/boot/
}

do_configure_prepend() {
    ( cd ${S}
      ${S}/autogen.sh )
}
