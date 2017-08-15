#!/bin/bash
# This test validates that the i915 driver firmware is installed and that its
# module is loaded and in use. It is skipped if it is run on an 'arm7l' device.

source $(dirname "$0")/ptest-format.sh

ptest_test=$(basename "$0" ".sh")  # test_i915_firmware.sh

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

# Use dmidecode to check the system-manufacturer is 'National Instruments'.
# If it is not, skip this test.
function check_manufacturer () {
	MAN=$(dmidecode --string system-manufacturer) \
	|| echo "ERROR: dmicode call failed." 

	if [ ! "${MAN}" == "National Instruments" ]; then
		echo "REASON: Detected system manufacturer as '${MAN}'."
		echo "REASON: We do not know if i915 should be tested."
		ptest_skip
	fi
}

# Passes iff the i915 module is loaded AND has >0 users. Fails otherwise.
function check_module () {
	MOD=$(lsmod | awk '/^i915/ {print $3}')
	if [ -z "$MOD" ]; then
		echo "i915 module not loaded."
		ptest_fail
		return
	fi

	echo -n "i915 module loaded."
	if [ ${MOD:='0'} -gt 0 ]; then
		ptest_pass
	else
		ptest_fail
	fi
	echo "${MOD} users."
}

check_arch
check_manufacturer

if [ $ptest_rc -ne 77 ]; then
	check_files
	check_module
fi

ptest_report
exit $ptest_rc
