
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://Add-align_kernel-macro.patch \
            file://0001-iproute2-fix-build-for-older-systems.patch"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS_{PN} += "niacctbase"

# openvpn is a priviledge-dropped daemon which needs to configure the
# network using ip.iproute2 (/sbin/ip symlink). Add 'ip' to the network
# group in which only openvpn is part of and chmod it to setuid & correct
# others permissions. In the future migrate to sudo and remove the setuid?
do_install_append() {
    chown 0:network ${D}${base_sbindir}/ip.iproute2
}
