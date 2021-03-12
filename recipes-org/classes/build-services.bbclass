# Helper class for Build Services communication / integration
#

MNT_BALTIC_PENGUINEXPORTS=""
MNT_NIRVANA_PERFORCEEXPORTS=""

detect_export_path() {
    EXPORT_PATH="$1"

    EXPORT_PREFIX=$(echo "$EXPORT_PATH" | cut -d'/' -f1)
    NIRVANA_EXPORT="$MNT_NIRVANA_PERFORCEEXPORTS/$EXPORT_PREFIX"
    BALTIC_EXPORT="$MNT_BALTIC_PENGUINEXPORTS/$EXPORT_PREFIX"

    # exception for ThirdPartyExports bc it is on both nirvana/baltic
    if [ "$EXPORT_PREFIX" = "ThirdPartyExports" ]; then
        EXPORT_PREFIX=$(echo "$EXPORT_PATH" | cut -d'/' -f2)
        NIRVANA_EXPORT="$MNT_NIRVANA_PERFORCEEXPORTS/ThirdPartyExports/$EXPORT_PREFIX"
        BALTIC_EXPORT="$MNT_BALTIC_PENGUINEXPORTS/ThirdPartyExports/$EXPORT_PREFIX"
    fi

    if [ -z "$EXPORT_PREFIX" ]; then
        echo "$EXPORT_PATH"
    elif [ -d "$BALTIC_EXPORT" ]; then
        echo "$MNT_BALTIC_PENGUINEXPORTS/$1"
    elif [ -d "$NIRVANA_EXPORT" ]; then
        echo "$MNT_NIRVANA_PERFORCEEXPORTS/$1"
    fi
}

bs_get_latest_export() {
    get_mount_points
    EXPORT_PATH="$1"
    EXPORT_FULL_PATH=$(detect_export_path $EXPORT_PATH)
    for phase in f b a d ; do
        latest_export=$(find $EXPORT_FULL_PATH -maxdepth 1 -regex ".*[0-9]+.[0-9]+.[0-9]+${phase}[0-9]+$" -type d|sort -V |tail -n 1)
        [ -n "$latest_export" ] && break
    done
    echo "$latest_export"
}

bs_get_latest_final() {
    get_mount_points
    EXPORT_PATH="$1"
    EXPORT_FULL_PATH=$(detect_export_path $EXPORT_PATH)
    find $EXPORT_FULL_PATH -maxdepth 2 -regex ".*[0-9]+.[0-9]+.[0-9]+f[0-9]+$" -type d|sort -V |tail -n 1
}

get_mount_points() {
    if [ -z "$MNT_BALTIC_PENGUINEXPORTS" ]; then
        MNT_BALTIC_PENGUINEXPORTS=$(mount | grep '^//baltic.*/penguinExports' | cut -d' ' -f3)
        if [ -z "$MNT_BALTIC_PENGUINEXPORTS" ]; then
            echo "ERROR: Baltic exports are not mounted, please mount using something like: ${NILRT_BALTIC_CMD}"
            exit 1
        fi
    fi
    if [ -z "$MNT_NIRVANA_PERFORCEEXPORTS" ]; then
        MNT_NIRVANA_PERFORCEEXPORTS=$(mount | grep '^//nirvana.*/perforceExports' | cut -d' ' -f3)
        if [ -z "$MNT_NIRVANA_PERFORCEEXPORTS" ]; then
            echo "ERROR: Nirvana exports are not mounted, please mount using something like: ${NILRT_BALTIC_CMD}"
            exit 1
        fi
    fi
}

# exports are fetched according to the "EXPORTS_TO_FETCH" var, which contains
# a whitespace separated list of export paths. Example:
# EXPORTS_TO_FETCH = "\
#    nilinux/bootloader/grub2/export/2.0/2.0.0f0/targets/linuxU/x64/gcc-4.3/release/smasher_grub \
#"
do_fetch() {
    get_mount_points

    PATHS_TO_SYNC=""
    for exp in ${EXPORTS_TO_FETCH}
    do
        EXPORT_FULL_PATH=$(detect_export_path $exp)
        PATHS_TO_SYNC="$PATHS_TO_SYNC $EXPORT_FULL_PATH"
    done

    if [ ! -z "$PATHS_TO_SYNC" ]; then
        mkdir -p "${BS_EXPORT_DATA}"
        rsync -a ${PATHS_TO_SYNC} "${BS_EXPORT_DATA}"

        if [ $? -ne 0 ]; then
            echo "ERROR: Could not copy files from remote export ${PATH_TO_SYNC}"
            exit 1
        fi
    else
        echo "WARNING: Could not find any paths to copy from exports"
    fi
}

do_clean() {
    rm -rf "${BS_EXPORT_DATA}"
}

# cmd options to use for mounting exports
MOUNT_OPTS="-o sec=ntlm,user=USERNAME,dom=DOMAIN,uid=LOCAL_USER,gid=LOCAL_GROUP,file_mode=0775,dir_mode=0775,password=PASSWORD"
NILRT_BALTIC_CMD="mount //baltic.natinst.com/penguinExports <filesystem-path> ${MOUNT_OPTS}"
NILRT_NIRVANA_CMD="mount //nirvana.natinst.com/perforceExports <filesystem-path> ${MOUNT_OPTS}"

# stores all exports to fetch to "$S"
EXPORTS_TO_FETCH=""

BS_EXPORT_DATA = "${TMPDIR}/build-services-export-data/${PN}_${PV}"

do_fetch[depends] += "util-linux-native:do_populate_sysroot rsync-native:do_populate_sysroot"
