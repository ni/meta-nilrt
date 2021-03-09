#!/bin/bash
#
# Test that the booted storage disk matches the partition scheme which we
# expect from the ni_provisioning script. This ptest only supports the
# bootscheme present on x64, safemode-based targets.
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
	fi
	if [[ ! "${uuid}" =~ $correct_re_uuid ]]; then
		print_error "Partition ${label} @ ${device} does not match required type UUID ${correct_re_uuid}"
	fi
}


found_error=false

if uname --machine | grep -q arm; then
	echo "INFO: detected an ARM machine architecture." \
		  "This ptest is only valid for non-ARM, safemode systems."
	ptest_skip
fi


## PARTITION TESTS ##
echo "## Checking that safemode partitons are present..."
# The UUIDs here should match those set by the disk_config_x64 file.
# https://github.com/ni/meta-nilrt/blob/983d50796302b394aa428b992dae48b4da32ff08/recipes-core/initrdscripts/files/disk_config_x64#L28
RE_ESP_UUID=C12A7328\-F81F\-11D2\-BA4B\-00A0C93EC93B
RE_X64_OTHER_UUID=0FC63DAF\-8483\-4772\-8E79\-3D69D8477DE4

# Determine the configured /boot storage device. Assert that this device should
# be the root of the partitions we evaluate in the remainder of this test.
boot_part=$(findmnt --kernel --first-only --noheadings --output SOURCE /boot)
test -n "${boot_part}" || { print_error "No detected /boot mount; system state invalid."; ptest_fail; }
boot_dev=/dev/$(lsblk -no PKNAME ${boot_part})
test -e "${boot_dev}" || { print_error "Boot device ${boot_dev} does not exist; system state invalid."; ptest_fail; }


readarray -t parts < <(fdisk -l ${boot_dev} -o Device,Name,Type-UUID | grep -o -E '^\/dev\/.*')

# ASSERT: Only 4 partitions exist
expected_partitions=4
test ${#parts[@]} = $expected_partitions || { print_error "Expected $expected_partitions partitions, found ${#parts[@]}"; ptest_fail; }

# ASSERT: part 0 is: nigrub and is an ESP partition
test_part_id ${parts[0]} nigrub $RE_ESP_UUID
# ASSERT: part 1 is: nibootfs and is a data partition type
test_part_id ${parts[1]} nibootfs $RE_X64_OTHER_UUID
# ASSERT: part 2 is: niconfig and is a data partition type
test_part_id ${parts[2]} niconfig $RE_X64_OTHER_UUID
# ASSERT: part 3 is: nirootfs and is a data partition type
test_part_id ${parts[3]} nirootfs $RE_X64_OTHER_UUID

part_1=( ${parts[1]} )
test $boot_part = ${part_1[0]} || { print_error "booted from ${boot_part} partition instead of ${part_1[0]} (nibootfs)"; ptest_fail; }

# bail out if the partition tests failed
$found_error && ptest_fail


## PARTITION CONTENTS TESTING ##
echo "## Checking that nibootfs contents are as expected..."
safemode_files=(                       \
	/boot/bootmode                      \
	/boot/.safe/bzImage                 \
	/boot/.safe/bootimage.ini           \
	/boot/.safe/EFI_NI_vars             \
	/boot/.safe/GRUB_NI_readonly_vars   \
	/boot/.safe/bootimage.cfg           \
	/boot/.safe/SMBIOS_NI_vars          \
	/boot/.safe/ramdisk.gz              \
	/boot/.safe/ramdisk.xz              \
	/boot/grub/recoverytool-ni-version  \
	/boot/grub/grub-ni-version          \
	/boot/grub/grubenv                  \
)

for file in ${safemode_files[@]}; do
	if [ -e "$file" ]; then
		echo "${file} [OK]"
	else
		echo "${file} [FAIL]"
		print_error "File \"$file\" does not exist."
	fi
done

$found_error && ptest_fail

ptest_pass
