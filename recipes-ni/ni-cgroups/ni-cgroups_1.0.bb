SUMMARY = "NI cgroup setup for LabVIEW Real Time"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS:${PN} = "bash coreutils gawk opkg update-rc.d"

inherit update-rc.d

SRC_URI = "file://ni-cgroups \
    file://ni-cgroups-v1 \
    file://ni-cgroups-v2 \
    "

INITSCRIPT_NAME = "ni-cgroups"
INITSCRIPT_PARAMS = "start 04 S . stop 04 0 6 ."

S = "${UNPACKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -Dm 0755 ${S}/ni-cgroups ${D}${sysconfdir}/init.d/
	install -Dm 0755 ${S}/ni-cgroups-v1 ${D}${sysconfdir}/init.d/
	install -Dm 0755 ${S}/ni-cgroups-v2 ${D}${sysconfdir}/init.d/
}
