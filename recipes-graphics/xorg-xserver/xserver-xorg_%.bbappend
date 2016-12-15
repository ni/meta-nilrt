FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://80-xorg-xrandr-hotplug.rules \
			"

PACKAGES =+" ${PN}-udev-rules "

RDEPENDS_${PN}-udev-rules =+ " ${PN} udev "

FILES_${PN}-udev-rules += "${sysconfdir}/udev/rules.d/80-xorg-xrandr-hotplug.rules"

do_install_append() {
    install -d ${D}${sysconfdir}/udev/rules.d/
    install -m 0755 ${WORKDIR}/80-xorg-xrandr-hotplug.rules ${D}${sysconfdir}/udev/rules.d/
}
