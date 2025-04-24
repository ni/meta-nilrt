FILESEXTRAPATHS:prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}:"

SRC_URI =+ " \
            file://busybox-ifplugd \
            file://ifplugd.action \
            file://ifplugd.conf \
            file://busybox-ifplugd.service \
            file://zcip.script \
            file://busybox-acpid \
            file://acpid.conf \
            file://acpid_poweroff.sh \
            file://acpid-logrotate.conf \
            file://busybox-acpid.service \
            file://0001-ifplugd.c-Increase-buffer-size-for-netlink-binding.patch \
            file://zcip-allow-action-script-to-reject-chosen-IP.patch \
            file://login-utilities.cfg \
            file://udhcpd.wlan0.conf"

SRC_URI:append:x64 = " file://enable_ar_create_fragment.cfg"

# we're using syslog-ng, don't build busybox syslog (avoids build warnings)
SRC_URI:remove = "file://syslog.cfg"
INITSCRIPT_PACKAGES:remove = "${PN}-syslog"

# Do not perform update-rc.d actions on the hwclock.sh initscript in this
# package. We only wish to call hwclock.sh from /etc/init.d/bootmisc manually.
INITSCRIPT_PACKAGES:remove = "${PN}-hwclock"

PACKAGES =+ " ${PN}-ifplugd"
PACKAGES =+ " ${PN}-acpid"
PACKAGES =+ " ${PN}-zcip"

DEPENDS =+ " libselinux"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${base_sbindir}',d)}"
FILES:${PN}-ifplugd = " \
	${script_location}/busybox-ifplugd \
	${sysconfdir}/ifplugd/ifplugd.action \
	${sysconfdir}/ifplugd/ifplugd.conf \
	${systemd_system_unitdir}/busybox-acpid.task \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_unitdir}/system/busybox-ifplugd.service','',d)} \
"
FILES:${PN}-acpid = " \
	${script_location}/busybox-acpid \
	${sysconfdir}/acpid.conf \
	${sysconfdir}/acpi \
	${sysconfdir}/acpi/poweroff.sh \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_unitdir}/system/busybox-acpid.service','',d)} \
"
FILES:${PN}-zcip = "${sysconfdir}/natinst/networking/zcip.script"
FILES:${PN}-udhcpd =+ "${sysconfdir}/udhcpd.wlan0.conf"
FILES:${PN}-hwclock =+ "${@bb.utils.contains('INIT_MANAGER','systemd','${base_sbindir}/hwclock.sh','',d)}"
FILES:${PN}-hwclock:remove = "${@bb.utils.contains('INIT_MANAGER','systemd','${sysconfdir}/init.d/hwclock.sh','',d)}"

INITSCRIPT_PACKAGES =+ "${@bb.utils.contains('INIT_MANAGER','sysvinit',' ${PN}-acpid','',d)}"
INITSCRIPT_NAME:${PN}-acpid = "busybox-acpid"
INITSCRIPT_PARAMS:${PN}-acpid = "start 20 2 3 4 5 . stop 20 0 1 6 ."
SYSTEMD_PACKAGES += "${@bb.utils.contains('INIT_MANAGER','systemd',' ${PN}-acpid','',d)}"
SYSTEMD_SERVICE:${PN}-acpid = "busybox-acpid.service"
SYSTEMD_AUTO_ENABLE:${PN}-acpid = "enable"

SYSTEMD_PACKAGES += "${@bb.utils.contains('INIT_MANAGER','systemd',' ${PN}-ifplugd','',d)}"
SYSTEMD_SERVICE:${PN}-ifplugd = "busybox-ifplugd.service"
SYSTEMD_AUTO_ENABLE:${PN}-ifplugd = "enable"

# Remove default busybox udhcpd init script; on NILRT images
# udhcpd is invoked directly from ifplugd action scripts
unset INITSCRIPT_NAME_${PN}-udhcpd
INITSCRIPT_PACKAGES:remove = "${PN}-udhcpd"

do_install:append () {
	if grep "CONFIG_IFPLUGD=y" ${B}/.config; then
		install -d ${D}${script_location}
		install -m 0755 ${WORKDIR}/busybox-ifplugd ${D}${script_location}

		install -d ${D}${sysconfdir}/ifplugd/
		install -m 0755 ${WORKDIR}/ifplugd.action ${D}${sysconfdir}/ifplugd/
		install -m 0755 ${WORKDIR}/ifplugd.conf ${D}${sysconfdir}/ifplugd/
		if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
			install -d ${D}${systemd_unitdir}/system
			install -m 0644 ${WORKDIR}/busybox-ifplugd.service ${D}${systemd_unitdir}/system/
		fi
	fi
	if grep "CONFIG_ACPID=y" ${B}/.config; then
		install -d ${D}${script_location}
		install -m 0755 ${WORKDIR}/busybox-acpid ${D}${script_location}

		install -m 0755 ${WORKDIR}/acpid.conf ${D}${sysconfdir}/admin
		install -d ${D}${sysconfdir}/acpi
		install -m 0755 ${WORKDIR}/acpid_poweroff.sh ${D}${sysconfdir}/acpi/poweroff.sh
		install -d ${D}${sysconfdir}/logrotate.d
		install -m 0644 ${WORKDIR}/acpid-logrotate.conf ${D}${sysconfdir}/logrotate.d/acpid.conf
		if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
			install -d ${D}${systemd_unitdir}/system
			install -m 0644 ${WORKDIR}/busybox-acpid.service ${D}${systemd_unitdir}/system/
		fi
	fi
	if grep "CONFIG_ZCIP=y" ${B}/.config; then
		install -d ${D}${sysconfdir}/natinst/networking
		install -m 0755 ${WORKDIR}/zcip.script ${D}${sysconfdir}/natinst/networking/zcip.script
	fi
	if grep "CONFIG_UDHCPD=y" ${B}/.config; then
		install -m 0644 ${WORKDIR}/udhcpd.wlan0.conf ${D}${sysconfdir}

		# Remove unused default busybox udhcpd init script
		rm -f ${D}${sysconfdir}/init.d/busybox-udhcpd
	fi
	if grep -q "CONFIG_HWCLOCK=y" ${B}/.config; then
		if ${@bb.utils.contains('INIT_MANAGER', 'systemd','true','false',d)}; then
			# Remove hwclock.sh from /etc/init.d
			rm -f ${D}${sysconfdir}/init.d/hwclock.sh
			install -m 0755 ${WORKDIR}/hwclock.sh ${D}${base_sbindir}
		fi
	fi
}
