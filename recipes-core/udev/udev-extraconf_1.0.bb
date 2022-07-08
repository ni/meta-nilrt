SUMMARY = "Extra machine specific configuration files"
DESCRIPTION = "Extra machine specific configuration files for udev, specifically blacklist information."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
	file://50-plugdev.rules \
	file://61-removable-storage-polling.rules \
	file://70-usb-serial-permissions.rules \
	file://automount.rules \
	file://hotplug.script \
	file://leds.rules \
	file://localextra.rules \
	file://localextra_rfkill.rules \
	file://mount.blacklist \
	file://mount.sh \
	file://net-hotplug.rules \
"

do_install() {
	install -d ${D}${sysconfdir}/udev/rules.d

	install -m 0644 ${WORKDIR}/automount.rules     ${D}${sysconfdir}/udev/rules.d/automount.rules
	install -m 0644 ${WORKDIR}/net-hotplug.rules   ${D}${sysconfdir}/udev/rules.d/net-hotplug.rules
	install -m 0644 ${WORKDIR}/50-plugdev.rules    ${D}${sysconfdir}/udev/rules.d/50-plugdev.rules
	install -m 0644 ${WORKDIR}/61-removable-storage-polling.rules    ${D}${sysconfdir}/udev/rules.d/61-removable-storage-polling.rules
	install -m 0644 ${WORKDIR}/70-usb-serial-permissions.rules ${D}${sysconfdir}/udev/rules.d/70-usb-serial-permissions.rules
	install -m 0644 ${WORKDIR}/leds.rules          ${D}${sysconfdir}/udev/rules.d/leds.rules

	install -m 0644 ${WORKDIR}/localextra.rules    ${D}${sysconfdir}/udev/rules.d/localextra.rules
	install -m 0644 ${WORKDIR}/localextra_rfkill.rules    ${D}${sysconfdir}/udev/rules.d/localextra_rfkill.rules

	install -m 0644 ${WORKDIR}/mount.blacklist     ${D}${sysconfdir}/udev/

	install -d ${D}${sysconfdir}/udev/scripts/

	install -m 0755 ${WORKDIR}/mount.sh            ${D}${sysconfdir}/udev/scripts/mount.sh

	install -m 0755 ${WORKDIR}/hotplug.script      ${D}${sysconfdir}/udev/scripts/hotplug.script

	cat >> ${D}${sysconfdir}/udev/rules.d/net-hotplug.rules << EOF

SUBSYSTEM=="net", ENV{networking}!="done", ENV{skipped}="yes"
SUBSYSTEM=="net", ENV{networking}=="done", ENV{skipped}="no", RUN+="/etc/udev/scripts/hotplug.script"
EOF
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} = "${sysconfdir}/udev"
RDEPENDS_${PN} = "udev"
CONFFILES_${PN} = "${sysconfdir}/udev/mount.blacklist"

# to replace udev-extra-rules from meta-oe
RPROVIDES_${PN} = "udev-extra-rules"
RREPLACES_${PN} = "udev-extra-rules"
RCONFLICTS_${PN} = "udev-extra-rules"

DEPENDS += "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} += "niacctbase"
