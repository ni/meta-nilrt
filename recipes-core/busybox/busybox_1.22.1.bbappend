
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}:"

SRC_URI =+ "file://automount.sh file://usb.sh"
SRC_URI =+ "file://busybox-cron file://cron.conf"
SRC_URI =+ "file://busybox-ifplugd"
SRC_URI =+ "file://busybox-acpid file://acpid.conf file://acpid_poweroff.sh"

PACKAGES =+ " ${PN}-cron"
PACKAGES =+ " ${PN}-ifplugd"
PACKAGES =+ " ${PN}-acpid"

DEPENDS =+ " libselinux"

FILES_${PN}-mdev += "${sysconfdir}/mdev ${sysconfdir}/mdev/automount.sh ${sysconfdir}/mdev/usb.sh "
FILES_${PN}-ifplugd = "${sysconfdir}/init.d/busybox-ifplugd"
FILES_${PN}-acpid = "${sysconfdir}/init.d/busybox-acpid ${sysconfdir}/acpid.conf ${sysconfdir}/acpi ${sysconfdir}/acpi/poweroff.sh"
FILES_${PN}-cron = "${sysconfdir}/init.d/busybox-cron ${sysconfdir}/cron/crontabs"

INITSCRIPT_PACKAGES =+ " ${PN}-cron ${PN}-ifplugd ${PN}-acpid"

INITSCRIPT_NAME_${PN}-ifplugd = "busybox-ifplugd"
INITSCRIPT_PARAMS_${PN}-ifplugd = "start 20 2 3 4 5 . stop 20 0 1 6 ."
INITSCRIPT_NAME_${PN}-acpid = "busybox-acpid"
INITSCRIPT_PARAMS_${PN}-acpid = "start 20 2 3 4 5 . stop 20 0 1 6 ."
INITSCRIPT_NAME_${PN}-cron = "busybox-cron"
INITSCRIPT_PARAMS_${PN}-cron = "start 20 2 3 4 5 . stop 20 0 1 6 ."

do_install_append () {
	if grep "CONFIG_CROND=y" ${B}/.config; then
		install -m 0755 ${WORKDIR}/busybox-cron ${D}${sysconfdir}/init.d/
		install -d ${D}${sysconfdir}/cron/crontabs
		install -d ${D}${sysconfdir}/logrotate.d
		install -m 644 ${WORKDIR}/cron.conf ${D}${sysconfdir}/logrotate.d/
	fi
	if grep "CONFIG_IFPLUGD=y" ${B}/.config; then
	       install -m 0755 ${WORKDIR}/busybox-ifplugd ${D}${sysconfdir}/init.d/
	fi
	if grep "CONFIG_ACPID=y" ${B}/.config; then
		install -m 0755 ${WORKDIR}/busybox-acpid ${D}${sysconfdir}/init.d/
		install -m 0755 ${WORKDIR}/acpid.conf ${D}${sysconfdir}/
		install -d ${D}${sysconfdir}/acpi
		install -m 0755 ${WORKDIR}/acpid_poweroff.sh ${D}${sysconfdir}/acpi/poweroff.sh
	fi
        if grep "CONFIG_MDEV=y" ${B}/.config; then
               if grep "CONFIG_FEATURE_MDEV_CONF=y" ${B}/.config; then
		       install -d ${D}${sysconfdir}/mdev
		       install -m 0755 ${WORKDIR}/automount.sh ${D}${sysconfdir}/mdev/automount.sh
		       install -m 0755 ${WORKDIR}/usb.sh ${D}${sysconfdir}/mdev/usb.sh
               fi
	fi
}
