DESCRIPTION = "Image used to provide the sysroot for the toolchains generated for cross-compilation"
LICENSE = "MIT"

inherit core-image
inherit populate_sdk

IPK_FEED_URIS=""
BUILD_IMAGES_FROM_FEEDS="0"

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_INSTALL = "\
    packagegroup-ni-base \
    packagegroup-ni-tzdata \
    "

# Clean-up dangling and escaping symlinks which would fail `tar -h` for
# Windows SDK builds. We should try to clean-up recipes that install
# broken files whenever possible instead of doing it here, but that's
# not an option in some cases.
create_sdk_files_append() {
    rm -f "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/etc/ld.so.cache"
    rm -f "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/etc/mtab"
    rm -f "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/var/lock"
    rm -f "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/lsb/install_initd"
    rm -f "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/lsb/remove_initd"
}
