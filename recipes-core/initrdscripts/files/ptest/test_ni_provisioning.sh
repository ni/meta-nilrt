#!/bin/bash
#
# Test that the booted storage disk matches the partition scheme which we
# expect from the ni_provisioning script. This ptest only supports the efi-ab
# bootscheme present on new x64, RAUC-based targets.
exec 2>&1


PTEST_TEST='ni_provisioning'

print_error() {
	echo ERROR: $@ >&2
	found_error=true
}

ptest_fail() {
	echo "FAIL: $PTEST_TEST"
	exit 1
}

ptest_pass() {
	echo "PASS: $PTEST_TEST"
	exit 0
}

ptest_skip() {
	echo "SKIP: $PTEST_TEST"
	exit 77
}


test_part_id () {
	local device=${1}
	local label=${2}
	local uuid=${3}

	local correct_re_label=${4}
	local correct_re_uuid=${5}

	echo "test_part_id() ${@:1:3}"

	if [[ ! "${label}" =~ $correct_re_label ]]; then
		print_error "Partition ${label} @ ${device} does not match required label ${correct_re_label}"
		found_error=true
	fi
	if [[ ! "${uuid}" =~ $correct_re_uuid ]]; then
		print_error "Partition ${label} @ ${device} does not match required type UUID ${correct_re_uuid}"
		found_error=true
	fi
}


found_error=false

if uname --machine | grep -q arm; then
	echo "INFO: detected an ARM machine architecture." \
	     "This ptest is only valid for non-ARM, RAUC systems."
	ptest_skip
fi


## PARTITION TESTS ##
echo "## Checking that RAUC partitons are present..."
# The UUIDs here should match those set by the disk_config_x64 file.
# https://github.com/ni/meta-nilrt/blob/983d50796302b394aa428b992dae48b4da32ff08/recipes-core/initrdscripts/files/disk_config_x64#L28
RE_ESP_UUID=C12A7328\-F81F\-11D2\-BA4B\-00A0C93EC93B
RE_X64_ROOT_UUID=4F68BCE3\-E8CD\-4DB1\-96E7\-FBCAF984B709

# Determine the configured /boot storage device. Assert that this device should
# be the root of the partitions we evaluate in the remainder of this test.
# For RAUC systems, we expect this to be: /dev/sda, but it isn't required to be.
boot_part=$(findmnt --kernel --first-only --noheadings --output SOURCE /boot)
test -n "${boot_part}" || (print_error "No detected /boot mount; system state invalid."; ptest_fail)
boot_dev=/dev/$(lsblk -no PKNAME ${boot_part})
test -e "${boot_dev}" || (print_error "Boot device ${boot_dev} does not exist; system state invalid."; ptest_fail)


readarray -t parts < <(fdisk -l ${boot_dev} -o Device,Name,Type-UUID | grep -o -E '^\/dev\/.*')

# ASSERT: part 0 is: niboota and is an ESP partition
test_part_id ${parts[0]} niboota $RE_ESP_UUID
# ASSERT: part 1 is: nibootb and is an ESP partition
test_part_id ${parts[1]} nibootb $RE_ESP_UUID
# ASSERT: part 2 is: niuser and is any partition type
test_part_id ${parts[2]} niuser $RE_X64_ROOT_UUID

# bail out if the partition tests failed
$found_error && ptest_fail


## PARTITION CONTENTS TESTING ##
echo "## Checking that niboot* contents are similar..."
set -eo pipefail

# Unmount the read-only partitions and remove their empty directories
unmount_ro_partitions () {
	set +e
	for part in $@; do
		[ -d ${part} ] && umount -f ${part} && rmdir ${part}
	done
}

# After provisioning, niboota and nibootb should contain exactly the same contents.
mnt_a=$(mktemp -d --tmpdir $PTEST_TEST.niboota.XXXXX)
mnt_b=$(mktemp -d --tmpdir $PTEST_TEST.nibootb.XXXXX)

trap "unmount_ro_partitions ${mnt_a} ${mnt_b}; exit 99" EXIT

mount -o ro -L niboota ${mnt_a}
mount -o ro -L nibootb ${mnt_b}

diff --recursive ${mnt_a} ${mnt_b} || \
	print_error "niboot partition contents differ appreciably."
echo "----"

trap - EXIT
unmount_ro_partitions ${mnt_a} ${mnt_b}


## EFI VARIABLE TESTS ###
echo "## Checking EFI bootorder..."
found_niboota=false
found_nibootb=false

efibootmgr -v  # INFO
IFS=',' read -a efi_bootorder < <(efibootmgr | grep '^BootOrder: ' | cut -d' ' -f2)

echo "----"
# ASSERT: bootpriorities 0 and 1 are niboota and nibootb, in any order.
for bootnum in 0 1; do
	bootorder=${efi_bootorder[${bootnum}]}
	bootlabel=$(efibootmgr | grep "^Boot${bootorder}\*" | cut -d' ' -f2-)
	case $bootlabel in
		niboota)
			found_niboota=true
			echo 'INFO: Found niboota in bootorder.'
			;;
		nibootb)
			found_nibootb=true
			echo 'INFO: Found nibootb in bootorder.'
			;;
		*)
			print_error "efi_bootorder[${bootnum}] is (${bootorder}, \"${bootlabel}\"); not one of: niboota, nibootb"
			found_error=true
			;;
	esac
done
# fail if either a or b was not found
($found_niboota && $found_nibootb) || found_error=true

$found_error && ptest_fail


ptest_pass
