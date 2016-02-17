SUMMARY = "Create two itb files with the purpose of redirecting U-Boot to boot.scr to continue the bootflow. For compatibility with older U-Boot environments the names of the itbs remain the same linux_safemode.itb and linux_runmode.itb. Also create a third fake itb, called linux_migration.itb, which will be installed on a CG target instead of linux_runmode.itb to trigger the migration process."
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SRC_URI = "\
	file://bootscript-safemode.txt		\
	file://bootscript-default.txt		\
	file://bootscript-migrate.txt		\
	file://linux-default.its		\
	file://linux-safemode.its		\
	file://linux-migrate.its		\
	"
PR = "r1"

S = "${WORKDIR}"

do_install() {
	install -d ${D}/dist/safemode/fit
	install -d ${D}/dist/default/fit
	install -d ${D}/dist/migrate/fit
	install -m 0644 bootscript-safemode.txt ${D}/dist/safemode/fit
	install -m 0644 bootscript-default.txt ${D}/dist/default/fit
	install -m 0644 bootscript-migrate.txt ${D}/dist/migrate/fit
	install -m 0644 linux-safemode.its ${D}/dist/safemode/fit
	install -m 0644 linux-default.its ${D}/dist/default/fit
	install -m 0644 linux-migrate.its ${D}/dist/migrate/fit
	mkimage -f ${D}/dist/safemode/fit/linux-safemode.its ${D}/dist/safemode/fit/linux_safemode.itb
	mkimage -f ${D}/dist/default/fit/linux-default.its ${D}/dist/default/fit/linux_runmode.itb
	mkimage -f ${D}/dist/migrate/fit/linux-migrate.its ${D}/dist/migrate/fit/linux_migrate.itb
}

SYSROOT_PREPROCESS_FUNCS += "itb_preprocess"

itb_preprocess () {
	install -d ${SYSROOT_DESTDIR}boot
	install -m 0644 ${D}/dist/default/fit/linux_runmode.itb ${SYSROOT_DESTDIR}boot/linux_runmode.itb
	install -m 0644 ${D}/dist/safemode/fit/linux_safemode.itb ${SYSROOT_DESTDIR}boot/linux_safemode.itb
	install -m 0644 ${D}/dist/migrate/fit/linux_migrate.itb ${SYSROOT_DESTDIR}boot/linux_migrate.itb
}

FILES_${PN} = " /dist/* "
