FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://firewalld-enabled \
	${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://ni-firewall-enabled.conf', '', d)} \
"

# Order firewalld symmetrically around the network-facing services at boot.
#
# By default firewalld registers at priority 20 (S20/K20). On start that puts it
# after sshd (S09) and around avahi (S21) and the NI service hooks (S25), so a
# listener can accept traffic before the ruleset is loaded. On stop K20 tears
# firewalld down before systemWebServer (K35), avahi (K36) and nisvcloc (K39),
# while networking stays up until K80; with CleanupOnExit=yes that flushes the
# rules and leaves those ports exposed for the rest of the shutdown.
#
# D-Bus (S02) is firewalld's only hard runtime dependency, so start at S03 to
# come up before every listener, and stop at K81 (just after networking, K80)
# so the firewall is torn down only once the network path is already down.
INITSCRIPT_PARAMS = "start 03 2 3 4 5 . stop 81 0 1 6 ."

# Both init systems start firewalld only when SystemSettings firewall.enabled
# in ni-rt.ini is true. It is off by default, leaving the network unfiltered.
do_install:append () {
	install -D -m 0755 ${UNPACKDIR}/firewalld-enabled ${D}${libexecdir}/firewalld-enabled
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		dropin=${D}${systemd_system_unitdir}/firewalld.service.d/ni-firewall-enabled.conf
		install -D -m 0644 ${UNPACKDIR}/ni-firewall-enabled.conf "$dropin"
		sed -i -e 's|@LIBEXECDIR@|${libexecdir}|g' "$dropin"
	else
		initscript=${D}${sysconfdir}/init.d/firewalld
		sed -i \
			-e '/^[[:space:]]*start)$/a\
        ${libexecdir}/firewalld-enabled || { echo "firewalld is disabled (SystemSettings firewall.enabled)"; exit 0; }' \
			-e '/^[[:space:]]*restart)$/a\
        ${libexecdir}/firewalld-enabled || { echo "firewalld is disabled (SystemSettings firewall.enabled)"; "$0" stop; exit 0; }' \
			"$initscript"
		if ! grep -A1 -E '^[[:space:]]*start\)$' "$initscript" | grep -Fq '${libexecdir}/firewalld-enabled ||' || \
		   ! grep -A1 -E '^[[:space:]]*restart\)$' "$initscript" | grep -Fq '${libexecdir}/firewalld-enabled ||'; then
			bbfatal "could not add the firewall.enabled check to the firewalld init script"
		fi
	fi
}

FILES:${PN} += "${libexecdir}/firewalld-enabled ${systemd_system_unitdir}/firewalld.service.d/ni-firewall-enabled.conf"
