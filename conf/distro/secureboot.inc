# Secure Boot configuration for NILRT
# Include this file in your distro configuration to enable secure boot features
#

# Enable UEFI Secure Boot
DISTRO_FEATURES:append:x64 = " uefi-secure-boot"

# Enable kernel module signing (works without meta-integrity)
DISTRO_FEATURES:append = " module-signing"

# Enable integrity and IMA/EVM features (requires meta-integrity layer)
# Only enable if meta-integrity layer is present
DISTRO_FEATURES:append = " ${@bb.utils.contains('BBFILE_COLLECTIONS', 'integrity', 'integrity ima', '', d)}"

# IMA/EVM rootfs signing is disabled globally - enable it per-image if needed
# Many NILRT images are minimal/initramfs and don't support IMA/EVM signing
# To enable for specific images, add to the image recipe:
#   IMAGE_CLASSES += "ima-evm-rootfs"
# IMAGE_CLASSES += "${@bb.utils.contains('BBFILE_COLLECTIONS', 'integrity', 'ima-evm-rootfs', '', d)}"

# =====================================================================
# Key Management Configuration
# =====================================================================
# IMPORTANT: The paths below use debug keys from meta-security.
# These keys are PUBLIC and should NEVER be used in production!
#
# For production use, generate your own keys following these steps:
# 1. Create a secure directory for your keys (outside the build tree)
# 2. Run: bitbake openssl-native
# 3. Run: bitbake -c devshell openssl-native
# 4. In the devshell, navigate to your key directory and run:
#    ${INTEGRITY_BASE}/scripts/ima-gen-local-ca.sh
#    ${INTEGRITY_BASE}/scripts/ima-gen-CA-signed.sh
# 5. Update the paths below to point to your keys
# 6. Keep the private keys secure and never commit them to version control
# =====================================================================

# IMA/EVM keys for filesystem integrity (only used if meta-integrity layer is present)
IMA_EVM_KEY_DIR ?= "${@bb.utils.contains('BBFILE_COLLECTIONS', 'integrity', '${INTEGRITY_BASE}/data/debug-keys', '', d)}"
IMA_EVM_PRIVKEY ?= "${IMA_EVM_KEY_DIR}/privkey_ima.pem"
IMA_EVM_X509 ?= "${IMA_EVM_KEY_DIR}/x509_ima.der"
IMA_EVM_ROOT_CA ?= "${IMA_EVM_KEY_DIR}/ima-local-ca.pem"

# IMA policy - enforce signatures on all files
# Available policies:
#   - ima_policy_simple: Basic measurement only
#   - ima_policy_hashed: Measurement with hashing
#   - ima_policy_appraise_all: Full appraisal (most secure)
IMA_EVM_POLICY ?= "${@bb.utils.contains('BBFILE_COLLECTIONS', 'integrity', '${INTEGRITY_BASE}/recipes-security/ima_policy_appraise_all/files/ima_policy_appraise_all', '', d)}"

# Kernel module signing keys
# These should be different from IMA keys for better security
MODULE_SIG_KEY_DIR ?= "${@bb.utils.contains('BBFILE_COLLECTIONS', 'integrity', '${INTEGRITY_BASE}/data/debug-keys', '', d)}"
MODSIGN_PRIVKEY ?= "${MODULE_SIG_KEY_DIR}/privkey_modsign.pem"
MODSIGN_X509 ?= "${MODULE_SIG_KEY_DIR}/x509_modsign.crt"

# UEFI Secure Boot keys (for x64 platforms)
# For production, generate UEFI keys using:
#   - Platform Key (PK)
#   - Key Exchange Key (KEK)
#   - Database key (db)
#   - Forbidden database key (dbx)
# See documentation: https://www.kernel.org/doc/html/latest/admin-guide/module-signing.html
UEFI_SB_KEYS_DIR ?= "${@bb.utils.contains('BBFILE_COLLECTIONS', 'integrity', '${INTEGRITY_BASE}/data/debug-keys', '', d)}"

# =====================================================================
# Secure Boot Build Options
# =====================================================================

# Sign kernel modules during build
KERNEL_MODULE_SIG = "1"
KERNEL_MODULE_SIG_ALL = "1"
KERNEL_MODULE_SIG_HASH = "sha256"

# Additional security hardening
# Note: read-only-rootfs is commented out as it's incompatible with initramfs images
# and some runtime configuration. Enable per-image if needed.
# EXTRA_IMAGE_FEATURES:append = " read-only-rootfs"

# Ensure grub-efi is built with secure boot support
GRUB_BUILDIN:append = " pgp gcry_sha256 gcry_sha512 gcry_rsa"
