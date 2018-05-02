DESCRIPTION = "Image used to provide the sysroot for the toolchains generated for cross-compilation"
LICENSE = "MIT"

inherit core-image
inherit populate_sdk

IPK_FEED_URIS=""
BUILD_IMAGES_FROM_FEEDS="0"

IMAGE_INSTALL = "\
    packagegroup-ni-base \
    packagegroup-ni-tzdata \
    packagegroup-core-standalone-sdk-target \
    "

# Clean-up dangling and escaping symlinks which would fail `tar -h` for
# Windows SDK builds. We should try to clean-up recipes that install
# broken files whenever possible instead of doing it here, but that's
# not an option in some cases.
create_sdk_files_append() {


    bbnote "Host Sysroot: Remove escaping ld.so.cache symlinks"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/etc/ld.so.cache"

    bbnote "Host Sysroot: Remove escaping bin symlinks"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/bin/dmesg"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/bin/kill"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/bin/more"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/sbin/blockdev"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/sbin/hwclock"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/sbin/mkswap"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/sbin/nologin"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/sbin/pivot_root"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/sbin/switch_root"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/chrt"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/dmesg"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/eject"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/flock"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/hexdump"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/kill"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/last"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/lastb"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/logger"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/mesg"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/more"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/renice"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/setsid"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/taskset"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/utmpdump"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/bin/wall"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/lib/cpp"

    bbnote "Host Sysroot: Remove unneeded ca-certificates"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/etc/ssl/certs"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/lib/ssl/certs"
    rm -Rf "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/usr/share/ca-certificates"


    bbnote "Target Sysroot: Remove escaping symlinks"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/etc/mtab"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/var/lock"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/lsb/install_initd"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/lsb/remove_initd"

    bbnote "Target Sysroot: Remove unneeded ca-certificates"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/etc/ssl/certs"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/lib/ssl/certs"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/share/ca-certificates"

    bbnote "Target Sysroot: Remove unneeded bin dirs"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/bin"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/lib/cpp"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/sbin"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/bin"
    rm -Rf "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/usr/sbin"
}

# Error if SDK sysroots contain broken symlinks
CHECK_SDK_SYSROOTS = "1"
