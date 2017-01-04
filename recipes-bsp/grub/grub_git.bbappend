FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

inherit deploy

PV = "2.02+git${SRCPV}"
SRCREV = "${AUTOREV}"

GRUB_BRANCH = "nilrt/17.0"

SRC_URI = "\
        git://git.natinst.com/grub.git;branch=${GRUB_BRANCH} \
        file://grub.cfg \
        file://cfg \
"

GRUBPLATFORM = "efi"

# Grub always try to force soft float in recent grub versions, even on x64, and this
# conflicts with how the x64 OE toolchain is set up. The only solution is to cache
# the empty variable to empty instead of no or yes, which will fail the build.
# More details: http://git.savannah.gnu.org/cgit/grub.git/tree/configure.ac?id=ce95549cc54b5d6f494608a7c390dba3aab4fba7#n790
CACHED_CONFIGUREVARS += "grub_cv_target_cc_soft_float="

PACKAGES =+ "grub-efi-nilrt"

PROVIDES =+ " grub-efi-nilrt "

GRUB_IMAGE = "bootx64.efi"
GRUB_NILRT_IMAGE = "grubx64.efi"

GRUB_TARGET = "x86_64"

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

do_install_class-native() {
    install -d ${D}${bindir}
    install -m 755 grub-mkimage ${D}${bindir}
}

do_deploy() {
    grub-mkimage -c ../cfg -p /EFI/BOOT -d ./grub-core/ \
	               -O ${GRUB_TARGET}-efi -o ./${GRUB_IMAGE} \
	               ${GRUB_BUILDIN}
    install -m 644 ${B}/${GRUB_IMAGE} ${DEPLOYDIR}/${GRUB_IMAGE}
}

do_deploy_class-native() {
        :
}

addtask deploy after do_install before do_build

PACKAGES =+ "grub-editenv"

RDEPENDS_${PN}_class-target = "diffutils freetype grub-editenv"
RDEPENDS_${PN}_class-native = ""

FILES_grub-editenv = "${bindir}/grub-editenv"

BBCLASSEXTEND = "native"
