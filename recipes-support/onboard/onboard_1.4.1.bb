SUMMARY = "An onscreen keyboard"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://COPYING.GPL3;md5=8521fa4dd51909b407c5150498d34f4e"

DEPENDS += "gtk+3 hunspell libcanberra libxkbfile dconf python3-distutils-extra-native intltool-native"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "https://launchpad.net/onboard/1.4/${PV}/+download/${BPN}-${PV}.tar.gz \
           file://0001-pypredict-lm-Define-error-API-if-platform-does-not-h.patch \
           file://0002-onboard-onhover-seg-fault-fix.patch \
           file://0001-add-xfce-to-autostart-onlyshowin.patch \
           file://01-gnome-accessibility \
           file://NI.colors \
           file://NI.theme \
           file://onboard-defaults.conf \
           "
SRC_URI[sha256sum] = "01cae1ac5b1ef1ab985bd2d2d79ded6fc99ee04b1535cc1bb191e43a231a3865"

CXXFLAGS += "-Werror=declaration-after-statement"

inherit features_check setuptools3 pkgconfig gtk-icon-cache gsettings mime-xdg

REQUIRED_DISTRO_FEATURES = "x11"

FILES:${PN} += " \
    ${datadir}/dbus-1 \
    ${datadir}/icons \
    ${datadir}/gnome-shell \
    ${datadir}/help \
"

RDEPENDS:${PN} += " \
    ncurses \
    python3-dbus \
    python3-pycairo \
    python3-pygobject \
"

do_install:append() {
	install -Dm 0644 ${D}${PYTHON_SITEPACKAGES_DIR}${sysconfdir}/xdg/autostart/onboard-autostart.desktop ${D}${sysconfdir}/xdg/autostart/onboard-autostart.desktop

	install -Dm 0644 ${WORKDIR}/01-gnome-accessibility ${D}${sysconfdir}/dconf/db/local.d/01-gnome-accessibility
	install -Dm 0644 ${WORKDIR}/onboard-defaults.conf ${D}${sysconfdir}/onboard/onboard-defaults.conf

	install -Dm 0644 ${WORKDIR}/NI.colors ${D}${datadir}/onboard/themes/NI.colors
	install -Dm 0644 ${WORKDIR}/NI.theme ${D}${datadir}/onboard/themes/NI.theme
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
