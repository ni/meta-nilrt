SUMMARY = "Create two itb files with the purpose of redirecting U-Boot to boot.scr to continue the bootflow. For compatibility with older U-Boot environments the names of the itbs remain the same linux_safemode.itb and linux_runmode.itb. Also create two itb's for migration (linux_fw/bw_migrate.itb), which will be installed on targets as linux_runmode.itb to trigger the migration process."
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# next-safemode & next-runmode are used for backwards-compatibility
SRC_URI = "\
	file://bootscript-next-safemode.txt	\
	file://bootscript-next-runmode.txt	\
	file://bootscript-fw-migrate.txt	\
	file://bootscript-bw-migrate.txt	\
	file://linux-next-runmode.its		\
	file://linux-next-safemode.its		\
	file://linux-fw-migrate.its		\
	file://linux-bw-migrate.its		\
	"

S = "${WORKDIR}"

do_install() {
	install -d ${D}/dist/safemode/fit
	install -d ${D}/dist/runmode/fit
	install -d ${D}/dist/migrate/fit
	install -m 0644 bootscript-next-safemode.txt ${D}/dist/safemode/fit
	install -m 0644 bootscript-next-runmode.txt ${D}/dist/runmode/fit
	install -m 0644 bootscript-fw-migrate.txt ${D}/dist/migrate/fit
	install -m 0644 bootscript-bw-migrate.txt ${D}/dist/migrate/fit
	install -m 0644 linux-next-safemode.its ${D}/dist/safemode/fit
	install -m 0644 linux-next-runmode.its ${D}/dist/runmode/fit
	install -m 0644 linux-fw-migrate.its ${D}/dist/migrate/fit
	install -m 0644 linux-bw-migrate.its ${D}/dist/migrate/fit
	mkimage -f ${D}/dist/safemode/fit/linux-next-safemode.its ${D}/dist/safemode/fit/linux_next_safemode.itb
	mkimage -f ${D}/dist/runmode/fit/linux-next-runmode.its ${D}/dist/runmode/fit/linux_next_runmode.itb
	mkimage -f ${D}/dist/migrate/fit/linux-fw-migrate.its ${D}/dist/migrate/fit/linux_fw_migrate.itb
	mkimage -f ${D}/dist/migrate/fit/linux-bw-migrate.its ${D}/dist/migrate/fit/linux_bw_migrate.itb
}

SYSROOT_PREPROCESS_FUNCS += "itb_preprocess"

itb_preprocess () {
	install -d ${SYSROOT_DESTDIR}/boot
	install -m 0644 ${D}/dist/runmode/fit/linux_next_runmode.itb ${SYSROOT_DESTDIR}/boot/linux_next_runmode.itb
	install -m 0644 ${D}/dist/safemode/fit/linux_next_safemode.itb ${SYSROOT_DESTDIR}/boot/linux_next_safemode.itb
	install -m 0644 ${D}/dist/migrate/fit/linux_fw_migrate.itb ${SYSROOT_DESTDIR}/boot/linux_fw_migrate.itb
	install -m 0644 ${D}/dist/migrate/fit/linux_bw_migrate.itb ${SYSROOT_DESTDIR}/boot/linux_bw_migrate.itb
}

FILES_${PN} = " /dist/* "
