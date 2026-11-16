SUMMARY = "Default kernel sysctl hardening for NI Linux Real-Time"
DESCRIPTION = "Installs NILRT's default kernel hardening sysctls in /etc/sysctl.d/ \
and an init script that applies /etc/sysctl.d/*.conf at boot."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "\
	file://60-ni-hardening.conf \
	file://nisysctlhardening \
"

S = "${UNPACKDIR}"

DEPENDS += "update-rc.d-native"

inherit allarch

do_install () {
	install -d ${D}${sysconfdir}/sysctl.d
	install -m 0644 ${S}/60-ni-hardening.conf ${D}${sysconfdir}/sysctl.d/

	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nisysctlhardening ${D}${sysconfdir}/init.d/

	# Before networking comes up and before bootmisc.sh (S36) applies /etc/sysctl.conf.
	update-rc.d -r ${D} nisysctlhardening start 34 S .
}

FILES:${PN} = "\
	${sysconfdir}/sysctl.d/60-ni-hardening.conf \
	${sysconfdir}/init.d/nisysctlhardening \
	${sysconfdir}/rcS.d/S34nisysctlhardening \
"
CONFFILES:${PN} = "${sysconfdir}/sysctl.d/60-ni-hardening.conf"

RDEPENDS:${PN} += "busybox kmod"
