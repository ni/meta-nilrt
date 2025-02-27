SUMMARY = "ni-auth systemD workaround files"
DESCRIPTION = "Workaround for ni-auth systemD implementation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = " \
	file://niauth_systemd.service \
	file://niauth_systemd \
"

DEPENDS += " \
	update-rc.d-native \
"

inherit allarch

PACKAGES = "${PN}"

SYSTEMD_SERVICE:${PN} = "niauth_systemd.service"

# Create niauth.service and install to /etc/systemd/system
FILES:${PN} += " \
	${sysconfdir}/systemd/system/niauth_systemd.service \
	/usr/local/natinst/share/NIAuth/niauth_systemd \
"

S = "${WORKDIR}"

# Create folder /etc/natinst/share/niauth
do_install () {
	install -d ${D}${sysconfdir}/systemd/system/
	install -m 0755 ${S}/niauth_systemd.service ${D}${sysconfdir}/systemd/system/

	install -d ${D}/usr/local/natinst/share/NIAuth/
	install -m 0755 ${S}/niauth_systemd ${D}/usr/local/natinst/share/NIAuth/
}

pkg_postinst_ontarget:${PN} () {
	/etc/init.d/niauth stop
	/sbin/update-rc.d -f niauth remove
	/bin/systemctl daemon-reload
	/bin/systemctl enable niauth_systemd.service
	/bin/systemctl start niauth_systemd.service
}
