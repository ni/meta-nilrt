FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://user \
"

CONFFILES_${PN}_append := " ${sysconfdir}/dconf/profile/user"

do_install_append () {
	install -d ${D}${sysconfdir}/dconf/db/local.d
	install -d ${D}${sysconfdir}/dconf/profile

	install -m 644 ${WORKDIR}/user ${D}${sysconfdir}/dconf/profile/
}

pkg_postinst_${PN} () {
	dconf update
}
