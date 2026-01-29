# Append to linux-nilrt kernel to add secure boot support

# Add secure boot kernel config fragment for x64 platforms
SRC_URI:append:x64 = " file://secureboot.cfg"

# Copy signing keys to kernel source before compilation
do_configure:prepend() {
    if [ -n "${MODSIGN_PRIVKEY}" ] && [ -f "${MODSIGN_PRIVKEY}" ]; then
        bbnote "Copying module signing key: ${MODSIGN_PRIVKEY} -> ${S}/certs/signing_key.pem"
        cp "${MODSIGN_PRIVKEY}" "${S}/certs/signing_key.pem"
        chmod 644 "${S}/certs/signing_key.pem"
    else
        bbwarn "MODSIGN_PRIVKEY not set or does not exist: ${MODSIGN_PRIVKEY}"
    fi

    if [ -n "${MODSIGN_X509}" ] && [ -f "${MODSIGN_X509}" ]; then
        bbnote "Copying module signing cert: ${MODSIGN_X509} -> ${S}/certs/signing_key.x509"
        cp "${MODSIGN_X509}" "${S}/certs/signing_key.x509"
        chmod 644 "${S}/certs/signing_key.x509"
    else
        bbwarn "MODSIGN_X509 not set or does not exist: ${MODSIGN_X509}"
    fi
}


# Enable module signing when secure boot is enabled
python () {
    distro_features = d.getVar('DISTRO_FEATURES') or ''
    if 'module-signing' in distro_features:
        # Set module signing keys if provided
        modsign_privkey = d.getVar('MODSIGN_PRIVKEY')
        modsign_x509 = d.getVar('MODSIGN_X509')

        # Enable module signing - actual keys will be copied during do_configure
        if modsign_privkey and modsign_x509:
            d.setVar('KERNEL_MODULE_SIG', '1')
            d.setVar('KERNEL_MODULE_SIG_ALL', '1')
            d.setVar('KERNEL_MODULE_SIG_HASH', 'sha256')
            bb.note('Module signing enabled with keys: %s, %s' % (modsign_privkey, modsign_x509))
        else:
            bb.warn('Module signing enabled but MODSIGN_PRIVKEY or MODSIGN_X509 not set')
}

# Enable IMA/EVM when integrity features are enabled
python () {
    distro_features = d.getVar('DISTRO_FEATURES') or ''
    if 'integrity' in distro_features:
        ima_x509 = d.getVar('IMA_EVM_X509')
        evm_x509 = d.getVar('IMA_EVM_X509')

        # Build IMA and EVM certificates into the kernel
        if ima_x509:
            d.setVar('IMA_X509_PATH', ima_x509)
            d.appendVar('KERNEL_EXTRA_ARGS', ' IMA_X509_PATH=%s' % ima_x509)

        if evm_x509:
            d.setVar('EVM_X509_PATH', evm_x509)
            d.appendVar('KERNEL_EXTRA_ARGS', ' EVM_X509_PATH=%s' % evm_x509)

        # Add IMA Root CA if provided
        ima_root_ca = d.getVar('IMA_EVM_ROOT_CA')
        if ima_root_ca:
            d.appendVar('KERNEL_EXTRA_ARGS', ' SYSTEM_TRUSTED_KEYS=%s' % ima_root_ca)
}

# For UEFI Secure Boot, build the kernel with EFI stub
do_deploy:append:x64() {
    if [ -e ${B}/arch/x86/boot/bzImage.efi ]; then
        install -m 0644 ${B}/arch/x86/boot/bzImage.efi ${DEPLOYDIR}/${KERNEL_IMAGETYPE}-efi-${PV}-${PR}-${MACHINE}.bin
        ln -sf ${KERNEL_IMAGETYPE}-efi-${PV}-${PR}-${MACHINE}.bin ${DEPLOYDIR}/${KERNEL_IMAGETYPE}-efi.bin
    fi
}

