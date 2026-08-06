SUMMARY = "Declarative firewalld drop-in contract and apply hook for NI software"
DESCRIPTION = "Provides /etc/ni/firewall/open.d and a boot-time hook that \
reconciles drop-in markers with firewalld (reusing ni-firewall-open), so any \
package can open a firewalld service in the default zone by shipping a service \
XML definition plus a marker file."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
S = "${UNPACKDIR}"
PV = "1.0"

SRC_URI = "\
	file://ni-firewall-apply \
	file://ni-firewall-apply.init \
"

inherit update-rc.d

INITSCRIPT_NAME = "ni-firewall-apply"
# Start after firewalld (installed with update-rc.d "defaults" => S20) so the
# reconciliation runs against the live daemon; no meaningful stop action.
INITSCRIPT_PARAMS = "start 25 2 3 4 5 . stop 75 0 1 6 ."

do_install () {
	install -D -m 0755 ${UNPACKDIR}/ni-firewall-apply ${D}${bindir}/ni-firewall-apply
	install -D -m 0755 ${UNPACKDIR}/ni-firewall-apply.init ${D}${sysconfdir}/init.d/ni-firewall-apply

	# The drop-in contract directory: other packages install markers here.
	install -d ${D}${sysconfdir}/ni/firewall/open.d
	# Persistent state directory for the apply hook.
	install -d ${D}${localstatedir}/lib/ni/firewall
}

FILES:${PN} += "${sysconfdir}/ni/firewall/open.d ${localstatedir}/lib/ni/firewall"

# Reuses the WI-A helper (ni-firewall-open/close) and firewalld's tools.
RDEPENDS:${PN} += "ni-firewalld-servicedefs firewalld firewalld-offline-cmd"
# flock serializes concurrent runs; depend on it so the lock is always present.
RDEPENDS:${PN} += "util-linux-flock"
