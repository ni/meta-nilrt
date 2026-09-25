DEPENDS += "openssl-native"

inherit user-key-store

fakeroot python do_sign() {
    import glob
    import os
    import re
    import shutil

    if (d.expand('${TARGET_ARCH}') != 'x86_64') and (not re.match('i.86', d.expand('${TARGET_ARCH}'))):
        return

    if d.expand('${UEFI_SB}') != '1':
        return

    kdest = d.expand('${D}/${KERNEL_IMAGEDEST}')
    sig_ext = d.expand('${SB_FILE_EXT}')

    if not sig_ext:
        bb.warn('SB_FILE_EXT is empty; skipping kernel secure-boot sidecar handling to avoid unsafe file operations.')
        return

    # Clean up stale artifacts produced by previous buggy runs so they
    # do not leak into packaging QA as installed-but-not-shipped files.
    for stale in glob.glob(os.path.join(kdest, '*' + sig_ext + sig_ext)):
        try:
            os.unlink(stale)
        except FileNotFoundError:
            pass

    for image_type in d.expand('${KERNEL_IMAGETYPES}').split():
        image_path = os.path.join(kdest, image_type)

        if os.path.exists(image_path):
            resolved_image = os.path.realpath(image_path)
        else:
            expected_image = os.path.join(kdest, image_type + '-' + d.expand('${KERNEL_RELEASE}'))
            if os.path.exists(expected_image):
                resolved_image = expected_image
            else:
                # NILRT kernels may be staged only as versioned files
                # (for example bzImage-<version>) before a stable symlink exists.
                # Fall back to a deterministic choice and still create image sidecars.
                candidates = [
                    c for c in glob.glob(os.path.join(kdest, image_type + '-*'))
                    if not c.endswith(sig_ext)
                ]
                if not candidates:
                    continue
                resolved_image = max(candidates, key=lambda p: (os.path.getmtime(p), p))

        # Keep an unsigned copy for manual verification/debugging and for
        # SELoader sidecar signing.
        unsigned_image = os.path.join(d.expand('${B}'), image_type + '.unsigned')
        shutil.copyfile(resolved_image, unsigned_image)

        if d.expand('${GRUB_SIGN_VERIFY}') == '1':
            sb_sign(resolved_image, resolved_image, d)

        # SELoader signature is always based on the unsigned kernel image,
        # disallowing chainloader to kernel efi-stub.
        uks_bl_sign(unsigned_image, d)

        resolved_sig = unsigned_image + sig_ext
        image_sig = image_path + sig_ext

        if os.path.exists(resolved_sig):
            versioned_sig = os.path.join(kdest, image_type + '-' + d.expand('${KERNEL_RELEASE}') + sig_ext)

            # If signing already emitted the signature with the expected
            # versioned filename, avoid copying a file onto itself.
            if os.path.abspath(resolved_sig) != os.path.abspath(versioned_sig):
                shutil.copyfile(resolved_sig, versioned_sig)

            if os.path.lexists(image_sig):
                os.unlink(image_sig)
            os.symlink(os.path.basename(versioned_sig), image_sig)
}

# Make sure the kernel image has been signed before kernel_do_deploy()
# which prepares kernel artifacts consumed by provisioning flows.
addtask sign after do_install before do_package do_populate_sysroot do_deploy

do_sign[prefuncs] += "check_deploy_keys"
do_sign[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"

do_deploy:append() {
    install -d "${DEPLOYDIR}/efi-unsigned"

    for imageType in ${KERNEL_IMAGETYPES}; do
        if [ -f "${B}/${imageType}.unsigned" ]; then
            install -m 0644 "${B}/${imageType}.unsigned" "${DEPLOYDIR}/efi-unsigned/${imageType}"
        fi

        if [ -n "${SB_FILE_EXT}" ] && [ -f "${D}/${KERNEL_IMAGEDEST}/${imageType}${SB_FILE_EXT}" ]; then
            install -m 0644 "${D}/${KERNEL_IMAGEDEST}/${imageType}${SB_FILE_EXT}" "${DEPLOYDIR}"
        fi
    done
}

# Ship *.p7b or *.sig files in the kernel image package.
python do_package:prepend() {
    sig_ext = d.expand('${SB_FILE_EXT}')
    if not sig_ext:
        bb.warn('SB_FILE_EXT is empty; skipping kernel-image sidecar FILES entries.')
        return

    for image_type in d.expand('${KERNEL_IMAGETYPES}').split():
        image_lower = image_type.lower()
        d.appendVar('FILES:kernel-image-' + image_lower,
                    ' ' + d.expand('${KERNEL_IMAGEDEST}/') + image_type + d.expand('-${KERNEL_RELEASE}${SB_FILE_EXT}'))
        d.appendVar('FILES:kernel-image-' + image_lower,
                    ' ' + d.expand('${KERNEL_IMAGEDEST}/') + image_type + d.expand('${SB_FILE_EXT}'))
}
