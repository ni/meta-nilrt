FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

SRC_URI += "\
	file://crond.init.older \
"

do_install_append() {
	# this only replaces the crond initscript for older nilrt to take into account
	# the value from ni-rt.ini for older nilrt's whether to enable/disable it)
	# this feature is undocumented and possibly never used in older nilrt's so
	# we should really check if it's needed and remove it if not
	if ${@base_conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
		install -m 0755 ${WORKDIR}/crond.init.older ${D}${sysconfdir}/init.d/crond
	fi
}
