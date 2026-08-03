# Original recipe's PV is too long causing feed exports to exceed path limits.
# So use a shortened SRCREV_moby in PV.
SRCREV_moby_short = "${@d.getVar('SRCREV_moby')[:10]}"
PV = "${DOCKER_VERSION}+git${SRCREV_moby_short}"

# Remove cgroup-lite dependency so that docker can work on systems with cgroup v2 paths.
RDEPENDS:${PN}:remove = "cgroup-lite"
RDEPENDS:${PN}:append = " ni-cgroups"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://daemon.json"

do_install:append() {
    install -d ${D}${sysconfdir}/docker
    install -m 0644 ${WORKDIR}/daemon.json \
        ${D}${sysconfdir}/docker/daemon.json
}

CONFFILES:${PN} += "${sysconfdir}/docker/daemon.json"
