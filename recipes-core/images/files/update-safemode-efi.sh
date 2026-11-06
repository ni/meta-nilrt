#!/bin/sh
# SPDX-License-Identifier: BSD-3-Clause
#
# Updates the `nigrub` EFI System Partition on an x64 NILRT target from a
# NILRT safemode archive (see README.md step 7). This script is bundled into
# the archive itself as /boot/.safe/update-safemode-efi.sh, so it can be run
# directly after extracting the archive with no arguments. It can also be
# copied to a target ahead of time and pointed at an unextracted archive with
# -a to extract it first. Run this on the target with Secure Boot disabled in
# firmware.
#
# Usage: update-safemode-efi.sh [-a archive.tar.gz] [esp-mount]

set -e

ARCHIVE=""
while getopts "a:" opt; do
    case "$opt" in
        a) ARCHIVE="$OPTARG" ;;
        *) echo "Usage: $0 [-a archive.tar.gz] [esp-mount]" >&2; exit 1 ;;
    esac
done
shift $((OPTIND - 1))

SAFE_DIR="/boot/.safe"
ESP_MOUNT="${1:-/mnt/esp}"
ESP_LABEL="nigrub"
BOOT_FILES="bootx64.efi SELoaderx64.efi mmx64.efi grubx64.efi Hash2DxeCrypto.efi Pkcs7VerifyDxe.efi LockDown.efi"

if [ -n "$ARCHIVE" ]; then
    tar xf "$ARCHIVE" -C "$SAFE_DIR"
fi

# The config sidecar extension follows SB_FILE_EXT (.p7b or .sig). Use the
# bootimage sidecar to select it, then require the matching GRUB sidecar too.
test -s "$SAFE_DIR/bootimage.cfg"
test -s "$SAFE_DIR/grub.cfg"
signature_ext=""
for ext in .p7b .sig; do
    if [ -s "$SAFE_DIR/bootimage.cfg$ext" ]; then
        signature_ext="$ext"
        break
    fi
done
if [ -z "$signature_ext" ]; then
    echo "No bootimage.cfg signature (.p7b or .sig) found in $SAFE_DIR." >&2
    exit 1
fi
test -s "$SAFE_DIR/grub.cfg$signature_ext"

for file in bootx64.efi mmx64.efi grubx64.efi; do
    if [ ! -s "$SAFE_DIR/efi/EFI/BOOT/$file" ]; then
        echo "Required EFI stage is missing or empty: $SAFE_DIR/efi/EFI/BOOT/$file" >&2
        exit 1
    fi
done

already_mounted=0
stage_dir=""
replacing=""
committed=0
cleanup() {
    status=$?
    trap - 0
    rollback_failed=0
    if [ "$committed" -eq 0 ] && [ -n "$stage_dir" ]; then
        for directory in boot nilrt; do
            if [ -d "$stage_dir/old-$directory" ]; then
                if ! { rm -rf "$ESP_MOUNT/efi/$directory" && mv "$stage_dir/old-$directory" "$ESP_MOUNT/efi/$directory"; }; then
                    rollback_failed=1
                fi
            else
                case " $replacing " in
                    *" $directory "*) rm -rf "$ESP_MOUNT/efi/$directory" || rollback_failed=1 ;;
                esac
            fi
        done
    fi
    if [ "$rollback_failed" -eq 0 ]; then
        if [ -n "$stage_dir" ]; then
            rm -rf "$stage_dir" || status=1
        fi
    else
        echo "EFI rollback failed; backups retained in $stage_dir." >&2
        status=1
    fi
    if [ "$already_mounted" -eq 0 ]; then
        umount "$ESP_MOUNT" || status=1
    fi
    exit "$status"
}

if mountpoint -q "$ESP_MOUNT"; then
    already_mounted=1
else
    mkdir -p "$ESP_MOUNT"
    mount -L "$ESP_LABEL" "$ESP_MOUNT"
fi
trap cleanup 0
trap 'exit 130' INT
trap 'exit 143' TERM

stage_dir=$(mktemp -d "$ESP_MOUNT/.safemode-efi.XXXXXX")
for directory in boot nilrt; do
    mkdir -p "$stage_dir/$directory"
    if [ -d "$ESP_MOUNT/efi/$directory" ]; then
        cp -a "$ESP_MOUNT/efi/$directory/." "$stage_dir/$directory/"
    fi
done

for file in $BOOT_FILES grub.cfg grub.cfg.p7b grub.cfg.sig; do
    rm -f "$stage_dir/boot/$file"
done
rm -f "$stage_dir/boot/fonts/unicode.pf2" "$stage_dir/boot/fonts/unicode.pf2.p7b"

for file in $BOOT_FILES; do
    if [ -f "$SAFE_DIR/efi/EFI/BOOT/$file" ]; then
        test -s "$SAFE_DIR/efi/EFI/BOOT/$file"
        cp -a "$SAFE_DIR/efi/EFI/BOOT/$file" "$stage_dir/boot/"
        cmp "$SAFE_DIR/efi/EFI/BOOT/$file" "$stage_dir/boot/$file"
    fi
done

for menu_dir in "$stage_dir/boot" "$stage_dir/nilrt"; do
    rm -f "$menu_dir/grub.cfg.p7b" "$menu_dir/grub.cfg.sig"
    cp -a "$SAFE_DIR/grub.cfg" "$menu_dir/"
    cp -a "$SAFE_DIR/grub.cfg$signature_ext" "$menu_dir/"
    cmp "$SAFE_DIR/grub.cfg" "$menu_dir/grub.cfg"
    cmp "$SAFE_DIR/grub.cfg$signature_ext" "$menu_dir/grub.cfg$signature_ext"
done

sync
mkdir -p "$ESP_MOUNT/efi"
for directory in boot nilrt; do
    if [ -d "$ESP_MOUNT/efi/$directory" ]; then
        mv "$ESP_MOUNT/efi/$directory" "$stage_dir/old-$directory"
    fi
    replacing="$replacing $directory"
    mv "$stage_dir/$directory" "$ESP_MOUNT/efi/$directory"
done
sync
committed=1

echo "EFI System Partition updated from $SAFE_DIR. Re-enable Secure Boot only after rebooting."
