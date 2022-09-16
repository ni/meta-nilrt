
# Clean-up dangling and escaping symlinks which would fail `tar -h` for
# Windows SDK builds. We should try to clean-up recipes that install
# broken files whenever possible instead of doing it here, but that's
# not an option in some cases.
create_sdk_files:append() {
    rm -f "${SDK_OUTPUT}/${SDKPATH}/sysroots/${SDK_SYS}/etc/ld.so.cache"
    rm -f "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/etc/mtab"
    rm -f "${SDK_OUTPUT}/${SDKTARGETSYSROOT}/var/lock"
}

# Error if SDK sysroots contain broken symlinks
CHECK_SDK_SYSROOTS = "1"
