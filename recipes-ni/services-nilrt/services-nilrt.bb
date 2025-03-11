SUMMARY = "SystemD nilrt Services"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

SRC_URI = "\
	file://cleanvarcache.service \
	file://firewall.service \
	file://firewall \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN} = "\
	cleanvarcache.service \
	firewall.service \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${systemd_unitdir}/system
	install -d ${D}${libdir}/systemd/scripts

	install -m 0644 ${WORKDIR}/cleanvarcache.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/firewall.service ${D}${systemd_unitdir}/system
	install -m 0755 ${WORKDIR}/firewall ${D}${libdir}/systemd/scripts

	# Substitute configfs paths
	sed -i 's|^IPTABLES_CONF=.*$|IPTABLES_CONF=/etc/natinst/share/iptables.conf|g' ${D}${libdir}/systemd/scripts/firewall
	sed -i 's|^IP6TABLES_CONF=.*$|IP6TABLES_CONF=/etc/natinst/share/ip6tables.conf|g' ${D}${libdir}/systemd/scripts/firewall
	# sanity check: break build if new _CONF vars exist which aren't substituted above
	! egrep '^[a-zA-Z0-9]*_CONF=.*$' ${D}${libdir}/systemd/scripts/firewall | egrep -v '^(IPTABLES_CONF)|(IP6TABLES_CONF)=.*$'
}

FILES:${PN} += " \
	${systemd_unitdir}/system/cleanvarcache.service \
	${systemd_unitdir}/system/firewall.service \
	${libdir}/systemd/scripts/firewall \
"

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
"
