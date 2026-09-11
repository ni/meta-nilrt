# Start firewalld before any network-facing service in the boot sequence.
#
# By default firewalld registers at start priority 20 (S20), which is after
# sshd (S09) and around other listeners such as avahi (S21) and the NI service
# hooks (S25). sshd begins listening well before the ruleset loads, leaving a
# window with no firewall rules in place. D-Bus (S02) is firewalld's only hard
# runtime dependency, so start firewalld at S03 to guarantee its rules are in
# place before every listener. Stop stays at 20 so firewalld shuts down before
# D-Bus (K37).
INITSCRIPT_PARAMS = "start 03 2 3 4 5 . stop 20 0 1 6 ."
