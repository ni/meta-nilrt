FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://init.d/ptest-runner"

FILES_${PN} += "${sysconfdir}/init.d/ptest-runner"

RDEPENDS_${PN} += "bash util-linux"

inherit update-rc.d
INITSCRIPT_NAME = "ptest-runner"
INITSCRIPT_PARAMS = "start 99 5 ."

do_install_append () {
	install -D -m 0755 ${WORKDIR}/init.d/ptest-runner ${D}${sysconfdir}/init.d/ptest-runner
}
