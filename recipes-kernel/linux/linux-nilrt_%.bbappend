require ${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'linux-nilrt-efi-secure-boot.inc', '', d)}
