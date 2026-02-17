FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://user \
"


do_install:append () {
	install -d ${D}${sysconfdir}/dconf/db/local.d
	install -d ${D}${sysconfdir}/dconf/profile

	install -m 644 ${UNPACKDIR}/user ${D}${sysconfdir}/dconf/profile/
}

pkg_postinst:${PN} () {
	dconf update
}

CONFFILES:${PN}:append := " ${sysconfdir}/dconf/profile/user"
