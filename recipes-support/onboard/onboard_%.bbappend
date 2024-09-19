FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# file://0001-add-xfce-to-autostart-onlyshowin.patch

SRC_URI += " \
	file://0001-add-xfce-to-autostart-onlyshowin.patch \
	file://onboard_hover_seg_fault.patch \
	file://01-gnome-accessibility \
	file://NI.colors \
	file://NI.theme \
	file://onboard-defaults.conf \
"

CFLAGS += "-Werror=declaration-after-statement"

do_install:append () {
	install -d ${D}${sysconfdir}/dconf/db/local.d
	install -d ${D}${sysconfdir}/onboard
	install -d ${D}${datadir}/onboard/themes

	install -m 644 ${WORKDIR}/01-gnome-accessibility ${D}${sysconfdir}/dconf/db/local.d/
	install -m 644 ${WORKDIR}/onboard-defaults.conf ${D}${sysconfdir}/onboard/

	install -m 644 ${WORKDIR}/NI.colors ${D}${datadir}/onboard/themes/
	install -m 644 ${WORKDIR}/NI.theme ${D}${datadir}/onboard/themes/
}

pkg_postinst:${PN} () {
	dconf update
}


CONFFILES:${PN}:append := " \
	${sysconfdir}/onboard/onboard-defaults.conf \
	${sysconfdir}/dconf/db/local.d/01-gnome-accessibility \
"

RDEPENDS:${PN}:append = " dconf"
# Onboard uses unicode glyphs in its key_defs.xml file, which means
# we need a font that has those glyphs present.
RDEPENDS:${PN}:append = " ttf-dejavu-sans"
