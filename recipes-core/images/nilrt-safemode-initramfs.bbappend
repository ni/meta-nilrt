require ${@bb.utils.contains('DISTRO_FEATURES', 'efi-secure-boot', 'nilrt-safemode-initramfs-efi-secure-boot.inc', '', d)}
