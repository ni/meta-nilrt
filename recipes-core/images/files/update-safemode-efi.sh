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
if mountpoint -q "$ESP_MOUNT"; then
    already_mounted=1
else
    mkdir -p "$ESP_MOUNT"
    mount -L "$ESP_LABEL" "$ESP_MOUNT"
fi

mkdir -p "$ESP_MOUNT/efi/boot"
for file in $BOOT_FILES grub.cfg grub.cfg.p7b grub.cfg.sig; do
    rm -f "$ESP_MOUNT/efi/boot/$file"
done
rm -f "$ESP_MOUNT/efi/boot/fonts/unicode.pf2" "$ESP_MOUNT/efi/boot/fonts/unicode.pf2.p7b"

for file in $BOOT_FILES; do
    if [ -f "$SAFE_DIR/efi/EFI/BOOT/$file" ]; then
        cp -a "$SAFE_DIR/efi/EFI/BOOT/$file" "$ESP_MOUNT/efi/boot/"
    fi
done

# Both cmp commands must produce no output.
for menu_dir in "$ESP_MOUNT/efi/boot" "$ESP_MOUNT/efi/nilrt"; do
    mkdir -p "$menu_dir"
    rm -f "$menu_dir/grub.cfg.p7b" "$menu_dir/grub.cfg.sig"
    cp -a "$SAFE_DIR/grub.cfg" "$menu_dir/"
    cp -a "$SAFE_DIR/grub.cfg$signature_ext" "$menu_dir/"
    cmp "$SAFE_DIR/grub.cfg" "$menu_dir/grub.cfg"
    cmp "$SAFE_DIR/grub.cfg$signature_ext" "$menu_dir/grub.cfg$signature_ext"
done

sync
if [ "$already_mounted" -eq 0 ]; then
    umount "$ESP_MOUNT"
fi

echo "EFI System Partition updated from $SAFE_DIR. Re-enable Secure Boot only after rebooting."
