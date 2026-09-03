# Enable UEFI capsule update support for NI Linux RT x64 targets
PACKAGECONFIG:append:x64 = " plugin_uefi_capsule plugin_uefi_pk"

# ESP mounting support for fwupdtool when polkit is enabled
RRECOMMENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'polkit', 'udisks2', '', d)}"
