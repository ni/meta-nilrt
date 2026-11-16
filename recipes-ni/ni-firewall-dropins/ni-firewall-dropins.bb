SUMMARY = "Declarative firewalld drop-in contract and apply hook for NI software"
DESCRIPTION = "Provides /etc/ni/firewall/open.d and a boot-time hook that \
reconciles drop-in markers with firewalld (reusing ni-firewall-open) after \
each opkg transaction and before firewalld starts, so any package can open a \
firewalld service by shipping a service XML definition plus a marker file. \
Declared services are opened in the ni-dropins firewalld policy, which applies\
applies to the public and work zones."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
S = "${UNPACKDIR}"
PV = "1.1"

SRC_URI = "\
	file://ni-firewall-apply \
	file://05-ni-firewall-apply \
	file://ni-dropins.xml \
"

# No init script of its own: the firewalld init script runs ni-firewall-apply
# before it starts the daemon (see the firewalld bbappend).
do_install () {
	install -D -m 0755 ${UNPACKDIR}/ni-firewall-apply ${D}${bindir}/ni-firewall-apply
	# Re-apply after each opkg transaction (needs opkg post_invoke support).
	install -D -m 0755 ${UNPACKDIR}/05-ni-firewall-apply ${D}${sysconfdir}/opkg/post_invoke.d/05-ni-firewall-apply

	# The drop-in contract directory: other packages install markers here.
	install -d ${D}${sysconfdir}/ni/firewall/open.d
	# Persistent state directory for the apply hook.
	install -d ${D}${localstatedir}/lib/ni/firewall

	# The policy the drop-ins are opened in. Shipped in the firewalld system
	# directory so that 'firewall-offline-cmd --reset-to-defaults' keeps it.
	install -D -m 0644 ${UNPACKDIR}/ni-dropins.xml \
		${D}${nonarch_libdir}/firewalld/policies/ni-dropins.xml
}

FILES:${PN} += "${sysconfdir}/ni/firewall/open.d ${localstatedir}/lib/ni/firewall ${sysconfdir}/opkg/post_invoke.d"
FILES:${PN} += "${nonarch_libdir}/firewalld/policies"

# Reuses the WI-A helper (ni-firewall-open/close, --policy since 1.1) and
# firewalld's tools.
RDEPENDS:${PN} += "ni-firewalld-servicedefs (>= 1.1) firewalld firewalld-offline-cmd"
# flock serializes concurrent runs; depend on it so the lock is always present.
RDEPENDS:${PN} += "util-linux-flock"
