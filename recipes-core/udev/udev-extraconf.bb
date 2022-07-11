SUMMARY = "NILRT distro-specific configuration files"
DESCRIPTION = "Extra machine specific configuration files for udev, specifically blacklist information."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.1"

SRC_URI = " \
	file://mount.blacklist \
	file://rules.d \
	file://scripts \
"

S = "${WORKDIR}"


udev_rules = "${D}${sysconfdir}/udev/rules.d"

do_install() {
	install -d ${D}${sysconfdir}/udev
	install -m 0644 ${S}/mount.blacklist ${D}${sysconfdir}/udev/

	install -d ${udev_rules}
	install -m 0644 ${S}/rules.d/automount.rules                 ${udev_rules}/automount.rules
	install -m 0644 ${S}/rules.d/leds.rules                      ${udev_rules}/leds.rules
	install -m 0644 ${S}/rules.d/localextra.rules                ${udev_rules}/localextra.rules
	install -m 0644 ${S}/rules.d/net-hotplug.rules               ${udev_rules}/net-hotplug.rules
	install -m 0644 ${S}/rules.d/plugdev.rules                   ${udev_rules}/50-plugdev.rules
	install -m 0644 ${S}/rules.d/removable-storage-polling.rules ${udev_rules}/61-removable-storage-polling.rules
	install -m 0644 ${S}/rules.d/usb-serial-permissions.rules    ${udev_rules}/70-usb-serial-permissions.rules

	install -d ${D}${sysconfdir}/udev/scripts
	install -m 0755 ${S}/scripts/* ${D}${sysconfdir}/udev/scripts
}


PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} = "${sysconfdir}/udev"
RDEPENDS:${PN} = "udev"
CONFFILES:${PN} = "${sysconfdir}/udev/mount.blacklist"

# to replace udev-extra-rules from meta-oe
RPROVIDES:${PN} = "udev-extra-rules"
RREPLACES:${PN} = "udev-extra-rules"
RCONFLICTS:${PN} = "udev-extra-rules"
