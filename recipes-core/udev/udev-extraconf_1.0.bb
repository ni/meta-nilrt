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

S = "${WORKDIR}"
udev_rules = "${D}${sysconfdir}/udev/rules.d"

do_install() {
	install -d ${D}${sysconfdir}/udev
	install -m 0644 ${S}/mount.blacklist ${D}${sysconfdir}/udev/

	install -d ${udev_rules}
	install -m 0644 ${S}/50-plugdev.rules                   ${udev_rules}/50-plugdev.rules
	install -m 0644 ${S}/61-removable-storage-polling.rules ${udev_rules}/61-removable-storage-polling.rules
	install -m 0644 ${S}/70-usb-serial-permissions.rules    ${udev_rules}/70-usb-serial-permissions.rules
	install -m 0644 ${S}/automount.rules                    ${udev_rules}/automount.rules
	install -m 0644 ${S}/leds.rules                         ${udev_rules}/leds.rules
	install -m 0644 ${S}/localextra.rules                   ${udev_rules}/localextra.rules
	install -m 0644 ${S}/localextra_rfkill.rules            ${udev_rules}/localextra_rfkill.rules
	install -m 0644 ${S}/net-hotplug.rules                  ${udev_rules}/net-hotplug.rules

	install -d ${D}${sysconfdir}/udev/scripts
	install -m 0755 ${S}/mount.sh       ${D}${sysconfdir}/udev/scripts/mount.sh
	install -m 0755 ${S}/hotplug.script ${D}${sysconfdir}/udev/scripts/hotplug.script
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
