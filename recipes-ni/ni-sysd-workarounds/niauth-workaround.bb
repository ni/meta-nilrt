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
	shadow-native \
	pseudo-native \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "niauth_systemd.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# Create niauth.service and install to //usr/lib/systemd/system
FILES:${PN} += " \
	${systemd_unitdir}system/niauth_systemd.service \
	/usr/local/natinst/share/NIAuth/niauth_systemd \
"

S = "${WORKDIR}"

# Create folder /etc/natinst/share/niauth
do_install () {
	install -d ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/niauth_systemd.service ${D}${systemd_unitdir}/system

	install -d ${D}/usr/local/natinst/share/NIAuth/
	install -m 0755 ${S}/niauth_systemd ${D}/usr/local/natinst/share/NIAuth/
}

pkg_preinst_on_target:${PN} () {
	/etc/init.d/niauth stop
	/sbin/update-rc.d -f niauth remove
}

RDEPENDS:${PN} += " \
	bash \
"
