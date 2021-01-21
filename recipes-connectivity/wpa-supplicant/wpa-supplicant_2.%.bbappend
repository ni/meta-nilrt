FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://wep-hexkeys-and-wpa-psk-hexkeys-fix.patch \
            file://report-eap-authentication-state.patch \
            file://dbus-wpa_supplicant.conf;subdir=nilrt \
            file://fi.epitest.hostap.WPASupplicant.service;subdir=nilrt \
            file://fi.w1.wpa_supplicant1.service;subdir=nilrt \
"

PACKAGECONFIG_append = " openssl"

do_install_append () {
	install -m 644 ${WORKDIR}/nilrt/dbus-wpa_supplicant.conf ${D}/${sysconfdir}/dbus-1/system.d
	install -m 644 ${WORKDIR}/nilrt/*.service ${D}/${datadir}/dbus-1/system-services
}
