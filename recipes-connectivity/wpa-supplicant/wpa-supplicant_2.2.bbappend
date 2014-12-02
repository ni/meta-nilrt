FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://wep-hexkeys-and-wpa-psk-hexkeys-fix.patch \
            file://report-eap-authentication-state.patch \
           "
