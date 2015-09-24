SUMMARY = "rtctl - Utilities for controlling scheduling priorities of system threads"
SECTION = "System Environment/Daemons"
LICENSE = "GPL"

# No license fine included in source. Hashing the "License" line in
# spec file.
LIC_FILES_CHKSUM = "file://${S}/rtctl.spec;beginline=6;endline=6;md5=cb929ed8d4eeb8a538033622343c6f94"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = " \
    http://git.centos.org/sources/rtctl/c7-rt/eb046c6fb02eff54d1907c2b003bbc076eed6ded \
    file://0001-rtctl-Read-task-IDs-from-procfs-instead-of-ps.patch \
    file://0002-rtctl-Support-multiple-rtgroups-files.patch \
"
SRC_URI[sha256sum] = "33706ea797f99054049c20d709ca0e5c8ae5daccf347b80e8ac2884266439101"
SRC_URI[md5sum] = "a530ceb797193c54b0d57a05b1e82d24"

S = "${WORKDIR}/rtctl-${PV}"

RDEPENDS_${PN} += "bash"

# Bitbake won't extract the src archive automatically because it does
# not have a file extension
do_unpack_append() {
    import tarfile
    with tarfile.open(name="${WORKDIR}/eb046c6fb02eff54d1907c2b003bbc076eed6ded", mode='r:bz2') as tar:
        tar.extractall(path="${WORKDIR}")
}

do_install() {
    install -m 0755 -d ${D}${sbindir}/
    install -m 0755 -d ${D}${sysconfdir}/rtgroups.d/

    install -m 0755 ${S}/rtctl ${D}${sbindir}/

    install -m 0644 ${S}/rtgroups ${D}${sysconfdir}/
}
