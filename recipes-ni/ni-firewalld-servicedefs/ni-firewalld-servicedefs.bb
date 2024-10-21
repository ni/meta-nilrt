SUMMARY = "Firewalld XML service definitions for NI software"
DESCRIPTION = "Installs firewalld service definitions for protocols implemented by NI software."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

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
	file://services/ni-rfsa-classic-sfp.xml \
	file://services/ni-rfsa-sfp.xml \
	file://services/ni-rfsg-sfp.xml \
	file://services/ni-scope-sfp.xml \
	file://services/ni-service-locator.xml \
	file://services/ni-sync-remote.xml \
	file://services/ni-visa-server.xml \
	file://services/ni-xnet-bus-monitor.xml \
	file://services/opcua.xml \
"

FILES:${PN} += "/"

do_install () {
	for f in ${SRC_URI}; do
		case $f in
		"file://services/"*) echo "$f"; install -D -t ${D}${libdir}/firewalld/services/ \
			-m 0644 "${WORKDIR}/${f##file://}" ;;
		esac
	done
}

RDEPENDS:${PN} += "firewalld"
