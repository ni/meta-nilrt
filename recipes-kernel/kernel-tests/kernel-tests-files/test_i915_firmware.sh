#!/bin/bash
# This test validates that the i915 driver firmware is installed and that its
# module is loaded and in use. It is skipped if it is run on an 'arm7l' device.

source $(dirname "$0")/ptest-format.sh

ptest_test=$(basename "$0" ".sh")  # test_i915_firmware.sh
ptest_rc=0

declare -xr SYSFS_PCI_DEVS="/sys/bus/pci/devices"

# Skips this test if we are on an ARM device.
function check_arch () {
	ARCH=$(uname -m)
	if [ "$ARCH" = "armv7l" ] ; then
		echo "REASON: i915 firmware is not installed on ARM targets"
		ptest_skip
	fi
}

# Checks /lib/firmware/i915 and various other files and links. If all exist,
# this check passes.
function check_files () {
	DIR_FIRMWARE='/lib/firmware/i915'
	if [ ! -d "${DIR_FIRMWARE}" ]; then
		echo "ERROR: ${DIR_FIRMWARE} not found."
		ptest_fail
	fi

	SYMS=( "bxt_dmc_ver1.bin" "kbl_dmc_ver1.bin" "skl_dmc_ver1.bin" "skl_guc_ver6.bin" )

	for s in "${SYMS[@]}" ; do
		if [ ! -h ${DIR_FIRMWARE}/$s ] ; then
			ptest_fail
		fi
	done

	FILES=( "bxt_dmc_ver1_07.bin" "kbl_dmc_ver1_01.bin" "skl_dmc_ver1_26.bin" "skl_guc_ver6_1.bin" )

	for f in "${FILES[@]}" ; do
		if [ ! -f ${DIR_FIRMWARE}/$f ] ; then
			ptest_fail
		fi
	done
}

function check_video_device () {
	has_video_device=0
	has_intel_video_device=0
	has_i915_driver_loaded=0

	# Enumerate PCI devices, looking for video controllers (that may
	# or may not have drivers bound to them)
	while read dev; do
		pci_class=$(cat $SYSFS_PCI_DEVS/$dev/class)
		# device class 0x03 is "display controller"
		pci_class_id=$(( ($pci_class & 0xFF0000) >> 16 ))
		if [ $pci_class_id -eq 3 ]; then
			has_video_device=1

			# get the vendor id
			vendor_id=$(cat $SYSFS_PCI_DEVS/$dev/vendor)
			if [ "${vendor_id}" = "0x8086" ]; then
				has_intel_video_device=1
			fi

			driver_name=$(basename $(realpath $SYSFS_PCI_DEVS/$dev/driver))
			if [ "${driver_name}" = "i915" ]; then
				has_i915_driver_loaded=1
				# we have one GPU with i915 loaded, we don't need
				# to look at any more devices.
				break
			fi
		fi
	done < <(ls $SYSFS_PCI_DEVS)

	if [ $has_video_device -eq 0 ]; then
		echo "REASON: No video devices found via PCI enumeration."
		ptest_skip
	elif [ $has_intel_video_device -eq 0 ]; then
		echo "REASON: No Intel video devices found via PCI enumeration."
		ptest_skip
	elif [ $has_intel_video_device -eq 1 -a $has_i915_driver_loaded -eq 0 ]; then
		echo "REASON: Intel video devices found, but i915 driver not attached."
		ptest_fail
	fi
}

check_arch
check_video_device

if [ $ptest_rc -ne 77 ]; then
	check_files
fi

ptest_report
exit $ptest_rc
