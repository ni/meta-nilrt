# The UEFI capsule plugin needs udisks2 to discover and mount the ESP. polkit is
# not a NILRT distro feature, so the upstream polkit-gated RRECOMMENDS is inert.
RDEPENDS:${PN}:append:x64 = " udisks2"
