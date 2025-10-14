# Original recipe's PV is too long causing feed exports to exceed path limits.
# So use a shortened SRCREV_moby in PV.
SRCREV_moby_short = "${@d.getVar('SRCREV_moby')[:10]}"
PV = "${DOCKER_VERSION}+git${SRCREV_moby_short}"
