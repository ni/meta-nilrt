FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://wep-hexkeys-and-wpa-psk-hexkeys-fix.patch \
            file://report-eap-authentication-state.patch \
            file://wpa_supplicant_conf \
            file://dbus-wpa_supplicant.conf \
"

PACKAGECONFIG_append = " openssl"

do_install_append() {
     install -d ${D}${sysconfdir}/network
     install -m 0755 ${WORKDIR}/wpa_supplicant_conf ${D}${sysconfdir}/network
     sed -i "s/Exec=\/usr\/sbin\/wpa_supplicant -u/Exec=\/usr\/sbin\/wpa_supplicant -Dnl80211 -i wlan0 -c \/etc\/natinst\/share\/wpa_supplicant.conf -u/" ${D}/${datadir}/dbus-1/system-services/fi.epitest.hostap.WPASupplicant.service
     sed -i "s/Exec=\/usr\/sbin\/wpa_supplicant -u/Exec=\/usr\/sbin\/wpa_supplicant -Dnl80211 -i wlan0 -c \/etc\/natinst\/share\/wpa_supplicant.conf -u/" ${D}/${datadir}/dbus-1/system-services/fi.w1.wpa_supplicant1.service

     # replace dbus-wpa_supplicant.conf with our own copy
     install -m 0644 ${WORKDIR}/dbus-wpa_supplicant.conf ${D}${sysconfdir}/dbus-1/system.d
}
