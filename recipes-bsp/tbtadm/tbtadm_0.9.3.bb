SUMMARY = "Thunderbolt user-space management tool"
DESCRIPTION = "tbtadm provides convenient way to interact with Thunderbolt kernel module, approve the connection of Thunderbolt devices, handle the ACL for auto-connecting devices and more."
HOMEPAGE = "https://github.com/intel/thunderbolt-software-user-space"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://COPYING;md5=565a8f58cc6472cf9c3ee9453d7e5aa4"

DEPENDS = "cmake-native udev boost"
RDEPENDS:${PN} = "boost-filesystem"

inherit cmake pkgconfig

SRCREV = "fe0fa2237b971ec8baae36b785d98a772684e5e7"
SRC_URI = "git://github.com/intel/thunderbolt-software-user-space.git;protocol=https;branch=master \
           file://0001-tbtadm-Disable-manpage-target.patch \
           "

S = "${WORKDIR}/git"

FILES:${PN} += "${datadir}/bash-completion/completions/tbtadm"

EXTRA_OECMAKE += "-DCMAKE_BUILD_TYPE=Release"
