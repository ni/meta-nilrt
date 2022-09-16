FILESEXTRAPATHS:prepend := "${THISDIR}:${THISDIR}/files:${THISDIR}/${PN}:"

SRC_URI =+ " \
			file://busybox-ifplugd \
			file://ifplugd.action \
			file://ifplugd.conf \
			file://zcip.script \
			file://busybox-acpid \
			file://acpid.conf \
			file://acpid_poweroff.sh \
			file://acpid-logrotate.conf \
			file://zcip-allow-action-script-to-reject-chosen-IP.patch \
			file://login-utilities.cfg \
			file://udhcpd.wlan0.conf"

SRC_URI:append:x64 += "file://enable_ar_create_fragment.cfg"

# we're using syslog-ng, don't build busybox syslog (avoids build warnings)
SRC_URI:remove += "file://syslog.cfg"
INITSCRIPT_PACKAGES:remove += "${PN}-syslog"

# Do not perform update-rc.d actions on the hwclock.sh initscript in this
# package. We only wish to call hwclock.sh from /etc/init.d/bootmisc manually.
INITSCRIPT_PACKAGES:remove = "${PN}-hwclock"

PACKAGES =+ " ${PN}-ifplugd"
PACKAGES =+ " ${PN}-acpid"
PACKAGES =+ " ${PN}-zcip"

DEPENDS =+ " libselinux"

FILES:${PN}-ifplugd = "${sysconfdir}/init.d/busybox-ifplugd ${sysconfdir}/ifplugd/ifplugd.action ${sysconfdir}/ifplugd/ifplugd.conf"
FILES:${PN}-acpid = "${sysconfdir}/init.d/busybox-acpid ${sysconfdir}/acpid.conf ${sysconfdir}/acpi ${sysconfdir}/acpi/poweroff.sh"
FILES:${PN}-zcip = "${sysconfdir}/natinst/networking/zcip.script"
FILES:${PN}-udhcpd =+ "${sysconfdir}/udhcpd.wlan0.conf"

INITSCRIPT_PACKAGES =+ " ${PN}-acpid"

INITSCRIPT_NAME:${PN}-acpid = "busybox-acpid"
INITSCRIPT_PARAMS:${PN}-acpid = "start 20 2 3 4 5 . stop 20 0 1 6 ."

# Remove default busybox udhcpd init script; on NILRT images
# udhcpd is invoked directly from ifplugd action scripts
# AB#2114544 Bitbake doesn't handle the new override syntax when unsetting a variable
unset INITSCRIPT_NAME_${PN}-udhcpd
INITSCRIPT_PACKAGES:remove = "${PN}-udhcpd"

do_install:append () {
	if grep "CONFIG_IFPLUGD=y" ${B}/.config; then
		install -d ${D}${sysconfdir}/ifplugd/
		install -m 0755 ${WORKDIR}/busybox-ifplugd ${D}${sysconfdir}/init.d/
		install -m 0755 ${WORKDIR}/ifplugd.action ${D}${sysconfdir}/ifplugd/
		install -m 0755 ${WORKDIR}/ifplugd.conf ${D}${sysconfdir}/ifplugd/
	fi
	if grep "CONFIG_ACPID=y" ${B}/.config; then
		install -m 0755 ${WORKDIR}/busybox-acpid ${D}${sysconfdir}/init.d/
		install -m 0755 ${WORKDIR}/acpid.conf ${D}${sysconfdir}/
		install -d ${D}${sysconfdir}/acpi
		install -m 0755 ${WORKDIR}/acpid_poweroff.sh ${D}${sysconfdir}/acpi/poweroff.sh
		install -d ${D}${sysconfdir}/logrotate.d
		install -m 0644 ${WORKDIR}/acpid-logrotate.conf ${D}${sysconfdir}/logrotate.d/acpid.conf
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
}
