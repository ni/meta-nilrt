FILESEXTRAPATHS_append := "${THISDIR}/files:"

SRC_URI_append = "\
	file://login_defs_uidgid.sed \
"

do_install_append() {
	# Change default dynamic uid/gid assignments for system users
	# so they do not conflict with those that OE statically assigns
	sed -i -f ${WORKDIR}/login_defs_uidgid.sed ${D}${sysconfdir}/login.defs
}
