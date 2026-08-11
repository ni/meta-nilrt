SUMMARY = "Firewalld XML service definitions for NI software"
DESCRIPTION = "Installs firewalld service definitions for protocols implemented by NI software."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
S = "${UNPACKDIR}"
PV = "1.0"

SRC_URI = "\
	file://services/dnp3.xml \
	file://services/dstp.xml \
	file://services/ethernet-ip-explicit.xml \
	file://services/ethernet-ip-implicit.xml \
	file://services/ethernet-ip.xml \
	file://services/iec-60870-5-104.xml \
	file://services/iec-61850.xml \
	file://services/modbus.xml \
	file://services/ni-dnet.xml \
	file://services/ni-imaq.xml \
	file://services/ni-labview-realtime.xml \
	file://services/ni-labview-viserver.xml \
	file://services/ni-logos-xt.xml \
	file://services/ni-mxs.xml \
	file://services/ni-rfsa-classic-sfp.xml \
	file://services/ni-rfsa-sfp.xml \
	file://services/ni-rfsg-sfp.xml \
	file://services/ni-rpc-server.xml \
	file://services/ni-scope-sfp.xml \
	file://services/ni-service-locator.xml \
	file://services/ni-sync-remote.xml \
	file://services/ni-visa-server.xml \
	file://services/ni-xnet-bus-monitor.xml \
	file://services/opcua.xml \
	file://ni-firewall-open \
"

FILES:${PN} += "/"

# Always-on NI services opened by this package on install and closed on removal.
# http/https are firewalld built-ins for the NI System Web Server (ports 80/443),
# which hosts Web-Based Configuration and the nisysapi/System Configuration channel
# used by NI MAX and Hardware Manager; they must stay reachable by default.
CORE_NI_SERVICES = "ni-service-locator ni-mxs ni-rpc-server ni-logos-xt ni-sync-remote http https"

do_install () {
	for f in ${SRC_URI}; do
		case $f in
		"file://services/"*) echo "$f"; install -D -t ${D}${libdir}/firewalld/services/ \
			-m 0644 "${UNPACKDIR}/${f##file://}" ;;
		esac
	done

	# Shared open/close helper; ni-firewall-close is a symlink to ni-firewall-open.
	install -D -m 0755 ${UNPACKDIR}/ni-firewall-open ${D}${bindir}/ni-firewall-open
	ln -sf ni-firewall-open ${D}${bindir}/ni-firewall-close
}

RDEPENDS:${PN} += "firewalld firewalld-offline-cmd"

# Open the always-on NI services once the package is on the target. Deferred to
# first boot (never runs against $D) so firewalld's tools operate natively.
pkg_postinst_ontarget:${PN} () {
	# Best effort: never let a firewall hiccup block package configuration.
	ni-firewall-open ${CORE_NI_SERVICES} || true
}

# Close them again on package removal, leaving no stale rules. Skip during any
# offline (image-build) removal.
pkg_prerm:${PN} () {
	if [ -n "$D" ]; then
		exit 0
	fi
	# Best effort: never let a firewall hiccup block package removal.
	ni-firewall-close ${CORE_NI_SERVICES} || true
}
