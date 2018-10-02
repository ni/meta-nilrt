SUMMARY = "Create scr files used by U-Boot to boot into different bootmodes."
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI ="\
	file://gen_bs.sh \
"

S = "${WORKDIR}"

DEPENDS = "u-boot-mkimage-native"

do_compile() {
	${S}/gen_bs.sh
	mkimage -T script -C none -n 'Top Level Bootscript' -d top_level_bootscript boot.scr
	mkimage -T script -C none -n 'Default Bootscript' -d default_bootscript default.scr
	mkimage -T script -C none -n 'Safemode Bootscript' -d safemode_bootscript safemode.scr
	mkimage -T script -C none -n 'Restore Mode Bootscript' -d restore_bootscript restore.scr
	mkimage -T script -C none -n 'BW Migration Bootscript' -d bw_migrate_bootscript backwards_migrate.scr
}

do_install() {
	install -d ${D}/boot
	install -m 0644 boot.scr ${D}/boot/boot.scr
	install -m 0644 default.scr ${D}/boot/default.scr
	install -m 0644 safemode.scr ${D}/boot/safemode.scr
	install -m 0644 restore.scr ${D}/boot/restore.scr
	install -m 0644 backwards_migrate.scr ${D}/boot/backwards_migrate.scr
}

SYSROOT_PREPROCESS_FUNCS += "bootscript_preprocess"

bootscript_preprocess () {
	install -d ${SYSROOT_DESTDIR}/boot
	install -m 0644 ${D}/boot/boot.scr ${SYSROOT_DESTDIR}/boot
	install -m 0644 ${D}/boot/default.scr ${SYSROOT_DESTDIR}/boot
	install -m 0644 ${D}/boot/safemode.scr ${SYSROOT_DESTDIR}/boot
	install -m 0644 ${D}/boot/restore.scr ${SYSROOT_DESTDIR}/boot
	install -m 0644 ${D}/boot/backwards_migrate.scr ${SYSROOT_DESTDIR}/boot
}

FILES_${PN} = "/boot/*"
