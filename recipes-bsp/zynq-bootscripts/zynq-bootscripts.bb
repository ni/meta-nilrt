SUMMARY = "Create scr files used by U-Boot to boot into different bootmodes."
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SRC_URI ="\
       file://gen_bs.sh \
           "
inherit deploy

S = "${WORKDIR}"

DEPENDS = "u-boot-mkimage-native"

do_compile() {
    ${S}/gen_bs.sh
	mkimage -T script -C none -n 'Top Level Bootscript' -d top_level_bootscript boot.scr
	mkimage -T script -C none -n 'Default Bootscript' -d default_bootscript default.scr
	mkimage -T script -C none -n 'Safemode Bootscript' -d safemode_bootscript safemode.scr
	mkimage -T script -C none -n 'Restore Mode Bootscript' -d restore_bootscript restore.scr
	mkimage -T script -C none -n 'BW Migration Bootscript' -d bw_migrate_bootscript bw-migrate.scr
}

SYSROOT_PREPROCESS_FUNCS += "bootscript_preprocess"

bootscript_preprocess(){
	install -d ${SYSROOT_DESTDIR}/boot
	install -m 0644 boot.scr ${SYSROOT_DESTDIR}/boot/boot.scr
	install -m 0644 default.scr ${SYSROOT_DESTDIR}/boot/default.scr
	install -m 0644 safemode.scr ${SYSROOT_DESTDIR}/boot/safemode.scr
	install -m 0644 restore.scr ${SYSROOT_DESTDIR}/boot/restore.scr
	install -m 0644 bw-migrate.scr ${SYSROOT_DESTDIR}/boot/bw-migrate.scr
}

FILES_${PN} = " restore.scr default.src boot.scr safemode.scr bw-migrate.scr"
