FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}:"

SRC_URI =+ " \
            file://busybox-ifplugd \
            file://ifplugd.action \
            file://busybox-acpid \
            file://acpid.conf \
            file://acpid_poweroff.sh \
            file://acpid-logrotate.conf \
	    file://zcip-allow-action-script-to-reject-chosen-IP.patch"

SRC_URI_append_x64 = "file://enable_ar_create_fragment.cfg"

# we're using syslog-ng, don't build busybox syslog (avoids build warnings)
SRC_URI_remove += "file://syslog.cfg"
INITSCRIPT_PACKAGES_remove += "${PN}-syslog"

PACKAGES =+ " ${PN}-ifplugd"
PACKAGES =+ " ${PN}-acpid"

DEPENDS =+ " libselinux"

FILES_${PN}-ifplugd = "${sysconfdir}/init.d/busybox-ifplugd ${sysconfdir}/ifplugd/ifplugd.action"
FILES_${PN}-acpid = "${sysconfdir}/init.d/busybox-acpid ${sysconfdir}/acpid.conf ${sysconfdir}/acpi ${sysconfdir}/acpi/poweroff.sh"

INITSCRIPT_PACKAGES =+ " ${PN}-ifplugd ${PN}-acpid"

INITSCRIPT_NAME_${PN}-ifplugd = "busybox-ifplugd"
INITSCRIPT_PARAMS_${PN}-ifplugd = "start 20 2 3 4 5 . stop 20 0 1 6 ."
INITSCRIPT_NAME_${PN}-acpid = "busybox-acpid"
INITSCRIPT_PARAMS_${PN}-acpid = "start 20 2 3 4 5 . stop 20 0 1 6 ."

do_install_append () {
	if grep "CONFIG_IFPLUGD=y" ${B}/.config; then
		install -d ${D}${sysconfdir}/ifplugd/
		install -m 0755 ${WORKDIR}/busybox-ifplugd ${D}${sysconfdir}/init.d/
		install -m 0755 ${WORKDIR}/ifplugd.action ${D}${sysconfdir}/ifplugd/
	fi
	if grep "CONFIG_ACPID=y" ${B}/.config; then
		install -m 0755 ${WORKDIR}/busybox-acpid ${D}${sysconfdir}/init.d/
		install -m 0755 ${WORKDIR}/acpid.conf ${D}${sysconfdir}/
		install -d ${D}${sysconfdir}/acpi
		install -m 0755 ${WORKDIR}/acpid_poweroff.sh ${D}${sysconfdir}/acpi/poweroff.sh
		install -d ${D}${sysconfdir}/logrotate.d
		install -m 0644 ${WORKDIR}/acpid-logrotate.conf ${D}${sysconfdir}/logrotate.d/acpid.conf
	fi
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
