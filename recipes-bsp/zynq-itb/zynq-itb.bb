SUMMARY = "Create itb files to direct U-Boot to load boot.scr for the NXG NILRT bootflow. On older NILRT, the legacy itb format is used (everything contained within the itb, no boot.scr outside it)."
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	file://forwards_migrate.txt		\
	file://forwards_migrate.its		\
	file://backwards_migrate.txt		\
	file://backwards_migrate.its		\
	file://nxg_redirect_runmode_boot.txt	\
	file://nxg_redirect_runmode_boot.its	\
	file://nxg_redirect_safemode_boot.txt	\
	file://nxg_redirect_safemode_boot.its	\
	"

S = "${WORKDIR}"

DEPENDS += "u-boot-mkimage-native dtc-native"

do_install() {
	install -d ${D}/fit

	install -m 0644 forwards_migrate.txt		${D}/fit
	install -m 0644 forwards_migrate.its		${D}/fit

	install -m 0644 backwards_migrate.txt		${D}/fit
	install -m 0644 backwards_migrate.its		${D}/fit

	install -m 0644 nxg_redirect_runmode_boot.txt	${D}/fit
	install -m 0644 nxg_redirect_runmode_boot.its	${D}/fit

	install -m 0644 nxg_redirect_safemode_boot.txt	${D}/fit
	install -m 0644 nxg_redirect_safemode_boot.its	${D}/fit

	mkimage -f ${D}/fit/forwards_migrate.its	${D}/fit/forwards_migrate.itb
	mkimage -f ${D}/fit/backwards_migrate.its	${D}/fit/backwards_migrate.itb

	mkimage -f ${D}/fit/nxg_redirect_safemode_boot.its	${D}/fit/nxg_redirect_safemode_boot.itb
	mkimage -f ${D}/fit/nxg_redirect_runmode_boot.its	${D}/fit/nxg_redirect_runmode_boot.itb
}

SYSROOT_PREPROCESS_FUNCS += "itb_preprocess"

itb_preprocess () {
	install -d ${SYSROOT_DESTDIR}/boot

	install -m 0644 ${D}/fit/nxg_redirect_safemode_boot.itb	${SYSROOT_DESTDIR}/boot/
	install -m 0644 ${D}/fit/nxg_redirect_runmode_boot.itb	${SYSROOT_DESTDIR}/boot/

	install -m 0644 ${D}/fit/forwards_migrate.itb	${SYSROOT_DESTDIR}/boot/
	install -m 0644 ${D}/fit/backwards_migrate.itb	${SYSROOT_DESTDIR}/boot/
}

FILES_${PN} = "/fit/*"
