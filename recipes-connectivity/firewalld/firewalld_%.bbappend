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

# Start firewalld only when SystemSettings firewall.enabled in ni-rt.ini is
# true; it is off by default, leaving the network unfiltered.
do_install:append () {
	initscript=${D}${sysconfdir}/init.d/firewalld
	sed -i \
		-e '/^\. \/etc\/init\.d\/functions$/a\
\
firewalld_enabled() {\
	nirtcfg=/usr/local/natinst/bin/nirtcfg\
	if [ ! -x "$nirtcfg" ]; then\
		echo "firewalld: $nirtcfg not found, cannot read SystemSettings firewall.enabled" >&2\
		return 1\
	fi\
	enabled=$("$nirtcfg" --get section=SystemSettings,token=firewall.enabled,value=false) || return 1\
	[ "$(echo "$enabled" | tr "[:upper:]" "[:lower:]")" = "true" ]\
}' \
		-e '/^[[:space:]]*start)$/a\
        firewalld_enabled || { echo "firewalld is disabled (SystemSettings firewall.enabled)"; exit 0; }' \
		-e '/^[[:space:]]*restart)$/a\
        firewalld_enabled || { echo "firewalld is disabled (SystemSettings firewall.enabled)"; "$0" stop; exit 0; }' \
		$initscript
	if ! grep -q '^firewalld_enabled() {$' $initscript || \
	   ! grep -A1 -E '^[[:space:]]*start\)$' $initscript | grep -q 'firewalld_enabled ||' || \
	   ! grep -A1 -E '^[[:space:]]*restart\)$' $initscript | grep -q 'firewalld_enabled ||'; then
		bbfatal "could not add the firewall.enabled check to the firewalld init script"
	fi
}
