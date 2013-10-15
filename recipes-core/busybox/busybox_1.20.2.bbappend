
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}-${PV}:"

SRC_URI =+ "file://busybox-ifplugd"

PACKAGES =+ " ${PN}-cron"
PACKAGES =+ " ${PN}-ifplugd"

FILES_${PN}-ifplugd = "${sysconfdir}/init.d/busybox-ifplugd"
FILES_${PN}-cron = "${sysconfdir}/init.d/busybox-cron ${sysconfdir}/cron/crontabs"

INITSCRIPT_PACKAGES =+ " ${PN}-cron ${PN}-ifplugd"

INITSCRIPT_NAME_${PN}-ifplugd = "busybox-ifplugd"
INITSCRIPT_PARAMS_${PN}-ifplugd = "start 20 2 3 4 5 . stop 20 0 1 6 ."
INITSCRIPT_NAME_${PN}-cron = "busybox-cron"
INITSCRIPT_PARAMS_${PN}-cron = "start 20 2 3 4 5 . stop 20 0 1 6 ."

do_install_append () {
	if grep "CONFIG_CROND=y" ${B}/.config; then
		install -m 0755 ${WORKDIR}/busybox-cron ${D}${sysconfdir}/init.d/
		install -d ${D}${sysconfdir}/cron/crontabs
	fi
	if grep "CONFIG_IFPLUGD=y" ${B}/.config; then
	       install -m 0755 ${WORKDIR}/busybox-ifplugd ${D}${sysconfdir}/init.d/
	fi
}
