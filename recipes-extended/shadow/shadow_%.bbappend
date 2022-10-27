FILESEXTRAPATHS:append := "${THISDIR}/files:"

include useradd-staticids-test.inc


SRC_URI:append = "\
	file://login_defs_uidgid.sed \
"

do_install:append() {
	# Change default dynamic uid/gid assignments for system users
	# so they do not conflict with those that OE statically assigns
	sed -i -f ${WORKDIR}/login_defs_uidgid.sed ${D}${sysconfdir}/login.defs
}
