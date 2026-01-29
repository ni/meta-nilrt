require grub-nilrt.inc

GRUB_BUILDIN += "smbios chain multiboot efi_uga font gfxterm gfxmenu terminal \
                minicmd iorw echo reboot terminfo loopback memdisk tar help serial \
                ls search_fs_uuid udf btrfs ntfs reiserfs xfs lvm ata \
                regexp probe"

# Add cryptographic modules for secure boot support
# Note: linuxefi was merged into linux module in GRUB 2.12+
GRUB_BUILDIN:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'uefi-secure-boot', \
    ' pgp gcry_sha256 gcry_sha512 gcry_rsa gcry_dsa \
      chain tpm luks luks2 cryptodisk', '', d)}"

# Downstream NI-branch code quality is not yet ready to build with -Werror
CFLAGS:append = " -Wno-error"

PACKAGES:prepend = "${PN}-nilrt "

do_install:append:class-target() {

    # Build NILRT grub image with prefix=\efi\nilrt instead of
    # the default \EFI\BOOT. We keep the upstream grub image
    # unchanged so that we may use it with USB provisioning tool
    # and other removable storage.
    (
    cd "${B}"
    grub-mkimage -p /efi/nilrt -d ./grub-core/ \
                 -O ${GRUB_TARGET}-efi -o ./${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} \
                 ${GRUB_BUILDIN}
    )

    # Install NILRT grub image
    install -d ${D}/boot/efi/nilrt
    install -m 644 ${B}/${GRUB_IMAGE_PREFIX}nilrt-${GRUB_IMAGE} ${D}/boot/efi/nilrt/${GRUB_IMAGE}

    # Install secure boot configuration if enabled
    if [ "${@bb.utils.contains('DISTRO_FEATURES', 'uefi-secure-boot', 'true', 'false', d)}" = "true" ]; then
        cat > ${D}/boot/efi/nilrt/secureboot.cfg << 'EOF'
# GRUB Secure Boot Configuration
# This file is sourced by the main grub configuration

# Enable secure boot verification
set check_signatures=enforce

# Trust only signatures from built-in certificates
trust --skip-sig

# Set secure defaults
set timeout=5
set default=0

# Disable editing of boot entries for security
set superusers="root"
# Production: set password with: password_pbkdf2 root <grub-mkpasswd-pbkdf2 hash>

EOF
    fi
}

FILES:${PN}-nilrt = "/boot/efi/nilrt/${GRUB_IMAGE}"
FILES:${PN}-nilrt:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'uefi-secure-boot', \
    ' /boot/efi/nilrt/secureboot.cfg', '', d)}"
