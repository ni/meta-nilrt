SUMMARY = "SystemD nilrt workaround files"
DESCRIPTION = "Workaround Files for SystemD implementation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = " \
	file://niauth.service \
	file://niauth_service \
	file://nirtmdnsd.service \
	file://niminionagent.service \
	file://nisvcloc.service \
	file://systemWebServer.service \
	file://systemWebServer_service \
	file://nisysmgmtmdnspublisher.service \
	file://init_script_removal \
"

DEPENDS += " \
	update-rc.d-native \
"

inherit allarch

PACKAGES = "${PN}"

# Create niauth.service and install to /etc/systemd/system
FILES:${PN} += " \
	${sysconfdir}/systemd/system/niauth.service \
	${sysconfdir}/systemd/system/nirtmdnsd.service \
	${sysconfdir}/systemd/system/niminionagent.service \
	${sysconfdir}/systemd/system/nisvcloc.service \
	${sysconfdir}/systemd/system/systemWebServer.service \
	${sysconfdir}/systemd/system/nisysmgmtmdnspublisher.service \
	/usr/local/natinst/share/NIAuth/niauth_service \
	/usr/local/natinst/share/NIWebServer/systemWebServer_service \
	/usr/local/natinst/share/init_script_removal \
"

S = "${WORKDIR}"

# Create folder /etc/natinst/share/niauth
do_install () {
	install -d ${D}${sysconfdir}/systemd/system/
	install -m 0755 ${S}/niauth.service ${D}${sysconfdir}/systemd/system/niauth.service
	install -m 0755 ${S}/nirtmdnsd.service ${D}${sysconfdir}/systemd/system/nirtmdnsd.service
	install -m 0755 ${S}/niminionagent.service ${D}${sysconfdir}/systemd/system/niminionagent.service
	install -m 0755 ${S}/nisvcloc.service ${D}${sysconfdir}/systemd/system/nisvcloc.service
	install -m 0755 ${S}/systemWebServer.service ${D}${sysconfdir}/systemd/system/systemWebServer.service
	install -m 0755 ${S}/nisysmgmtmdnspublisher.service ${D}${sysconfdir}/systemd/system/nisysmgmtmdnspublisher.service

	install -d ${D}/usr/local/natinst/share/NIAuth/
	install -d ${D}/usr/local/natinst/share/NIWebServer
	install -m 0755 ${S}/init_script_removal ${D}/usr/local/natinst/share/init_script_removal
	install -m 0755 ${S}/niauth_service ${D}/usr/local/natinst/share/NIAuth/niauth_service
	install -m 0755 ${S}/niauth_service ${D}/usr/local/natinst/share/NIWebServer/systemWebServer_service
}

pkg_postinst_ontarget:${PN} () {
	/usr/local/natinst/share/init_script_removal niauth
	/usr/local/natinst/share/init_script_removal nirtmdnsd
	# The execution of this seems to fail due to
   # "Error: Unable to load library: "libnidaqmx.so.1" ...
	# /usr/local/natinst/share/init_script_removal niminionagent
	/usr/local/natinst/share/init_script_removal nisvcloc
	# systemWebServer service is currently failing to execute
	# When running manually, error: "SystemWebServer: cannot execute binary file"
	# /usr/local/natinst/share/init_script_removal systemWebServer
	# nisysmgmtmdnspublisher service is currently failing to execute
	# "nismsmDNSPublisher: Unhandled exception: No such node (local.productcode)"
	# /usr/local/natinst/share/init_script_removal nisysmgmtmdnspublisher
}