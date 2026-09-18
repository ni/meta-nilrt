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

# bootimage.cfg's detached signature must exist in /boot/.safe; if missing,
# the safemode archive was not built with a matching Secure Boot signature.
test -s "$SAFE_DIR/bootimage.cfg"
test -s "$SAFE_DIR/bootimage.cfg.p7b"

already_mounted=0
if mountpoint -q "$ESP_MOUNT"; then
    already_mounted=1
else
    mkdir -p "$ESP_MOUNT"
    mount -L "$ESP_LABEL" "$ESP_MOUNT"
fi

mkdir -p "$ESP_MOUNT/efi/boot"
for file in $BOOT_FILES grub.cfg grub.cfg.p7b; do
    rm -f "$ESP_MOUNT/efi/boot/$file"
done
rm -f "$ESP_MOUNT/efi/boot/fonts/unicode.pf2" "$ESP_MOUNT/efi/boot/fonts/unicode.pf2.p7b"

for file in $BOOT_FILES; do
    if [ -f "$SAFE_DIR/efi/EFI/BOOT/$file" ]; then
        cp -a "$SAFE_DIR/efi/EFI/BOOT/$file" "$ESP_MOUNT/efi/boot/"
    fi
done

cp -a "$SAFE_DIR/grub.cfg" "$ESP_MOUNT/efi/boot/"
cp -a "$SAFE_DIR/grub.cfg.p7b" "$ESP_MOUNT/efi/boot/"

# Both cmp commands must produce no output.
cmp "$SAFE_DIR/grub.cfg" "$ESP_MOUNT/efi/boot/grub.cfg"
cmp "$SAFE_DIR/grub.cfg.p7b" "$ESP_MOUNT/efi/boot/grub.cfg.p7b"

sync
if [ "$already_mounted" -eq 0 ]; then
    umount "$ESP_MOUNT"
fi

echo "EFI System Partition updated from $SAFE_DIR. Re-enable Secure Boot only after rebooting."
