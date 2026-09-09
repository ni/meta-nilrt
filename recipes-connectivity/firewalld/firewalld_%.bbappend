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
