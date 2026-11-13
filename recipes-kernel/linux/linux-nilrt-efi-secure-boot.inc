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
            expected_image = os.path.join(kdest, image_type + '-' + d.expand('${KERNEL_VERSION}'))
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

        # Keep an unsigned copy for manual verification/debugging and deploy.
        unsigned_image = os.path.join(d.expand('${B}'), image_type + '.unsigned')
        shutil.copyfile(resolved_image, unsigned_image)

        if d.expand('${GRUB_SIGN_VERIFY}') == '1':
            sb_sign(resolved_image, resolved_image, d)

        # Sign the installed bytes so the detached sidecar matches the image
        # GRUB/SELoader will verify and load.
        uks_bl_sign(resolved_image, d)

        resolved_sig = resolved_image + sig_ext

        if os.path.exists(resolved_sig):
            versioned_sig = os.path.join(kdest, image_type + '-' + d.expand('${KERNEL_VERSION}') + sig_ext)

            # If signing already emitted the signature with the expected
            # versioned filename, avoid copying a file onto itself.
            if os.path.abspath(resolved_sig) != os.path.abspath(versioned_sig):
                shutil.copyfile(resolved_sig, versioned_sig)
}

# Make sure the kernel image has been signed before kernel_do_deploy()
# which prepares kernel artifacts consumed by provisioning flows.
addtask sign after do_install before do_package do_populate_sysroot do_deploy

do_sign[prefuncs] += "check_deploy_keys"
do_sign[prefuncs] += "${@'check_boot_public_key' if d.getVar('GRUB_SIGN_VERIFY') == '1' else ''}"

do_deploy:append() {
    if [ "${KERNEL_PACKAGE_NAME}" = "kernel" ]; then
        kernel_deploy_dir="${DEPLOYDIR}"
    else
        kernel_deploy_dir="${DEPLOYDIR}/${KERNEL_PACKAGE_NAME}"
    fi
    install -d "${kernel_deploy_dir}/efi-unsigned"

    for imageType in ${KERNEL_IMAGETYPES}; do
        if [ -f "${B}/${imageType}.unsigned" ]; then
            install -m 0644 "${B}/${imageType}.unsigned" "${kernel_deploy_dir}/efi-unsigned/${imageType}"
        fi

        # The packaged sidecar is versioned; deploy it under the stable name.
        if [ -n "${SB_FILE_EXT}" ] && [ -f "${D}/${KERNEL_IMAGEDEST}/${imageType}-${KERNEL_VERSION}${SB_FILE_EXT}" ]; then
            install -m 0644 "${D}/${KERNEL_IMAGEDEST}/${imageType}-${KERNEL_VERSION}${SB_FILE_EXT}" "${kernel_deploy_dir}/${imageType}${SB_FILE_EXT}"
        fi
    done
}

# Ship the versioned *.p7b or *.sig files in the kernel image package. The
# unversioned links are managed on the target by the -base package scripts, so
# packaging them here would conflict between installed kernels.
python do_package:prepend() {
    sig_ext = d.expand('${SB_FILE_EXT}')
    if not sig_ext:
        bb.warn('SB_FILE_EXT is empty; skipping kernel-image sidecar FILES entries.')
        return

    for image_type in d.expand('${KERNEL_IMAGETYPES}').split():
        image_lower = image_type.lower()
        d.appendVar('FILES:' + d.expand('${KERNEL_PACKAGE_NAME}') + '-image-' + image_lower,
                    ' ' + d.expand('${KERNEL_IMAGEDEST}/') + image_type + d.expand('-${KERNEL_VERSION}${SB_FILE_EXT}'))
}
