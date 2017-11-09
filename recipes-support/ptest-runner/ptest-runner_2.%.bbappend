FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

# Rev to jeffreyp's monotonic clock source patch to fix an issue with the
# salt-testing suite. This rev overwrite can be dropped once the ptest-runner
# package from upstream includes this commit.
SRCREV = "41b7f4814d39c1930b1fcf0be2e247a73546fb80"
PV = "2.1+git${SRCPV}"

SRC_URI =+ "file://init.d/ptest-runner"

FILES_${PN} += "${sysconfdir}/init.d/ptest-runner"

RDEPENDS_${PN} += "bash util-linux"

inherit update-rc.d
INITSCRIPT_NAME = "ptest-runner"
INITSCRIPT_PARAMS = "start 99 5 ."

do_install_append () {
	install -D -m 0755 ${WORKDIR}/init.d/ptest-runner ${D}${sysconfdir}/init.d/ptest-runner
}
