FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

SRC_URI += "\
	file://crond.init.newer \
	file://crond.init.older \
"

# these crond initscripts are here for legacy reasons and will be removed
# in the future (once the default system stack size limits will be removed).
# crond.init.older is used for older NILRT, cron.init.newer for newer NILRT
do_install_append() {
	if ${@base_conditional('DISTRO', 'nilrt-nxg', 'true', 'false', d)}; then
		install -m 0755 ${WORKDIR}/crond.init.newer ${D}${sysconfdir}/init.d/crond
	else
		install -m 0755 ${WORKDIR}/crond.init.older ${D}${sysconfdir}/init.d/crond
	fi
}
