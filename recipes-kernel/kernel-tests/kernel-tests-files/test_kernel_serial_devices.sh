#!/bin/bash

# This test checks to make sure that the set of serial devices that are
# present are those that we expect for this class of controller.
#
# In particular, we want to ensure that the legacy serial ports do not
# appear; when they do, this 1) renumbers /dev/ttySn and breaks users and
# 2) puts a bunch of ttySn devices at invalid addresses.
#
# (AzDO#1722341 is one case where we messed this up when we inadvertantly
# dropped CONFIG_SERIAL_8250_RUNTIME_UARTS=0 from the defconfig)
#
# This test only checks to see that we have serial ports at the right
# addresses; it does no other verification.

source $(dirname "$0")/ptest-format.sh

ptest_test=$(basename "$0" ".sh")

# Check the test machine architecture. If it is a supported arch for this test,
# return 0; otherwise, return 1.
function _check_machine_arch () {
	case $(uname -m) in
		x86_64)
			return 0
			;;
		*)
			echo "INFO: machine architecture not supported for this test"
			return 1
			;;
	esac
}

# Check the device vendor from DMI. Return 0 if this is an NI target;
# otherwise (e.g. if a VM)
function _check_machine_vendor_ni () {
	SYS_VENDOR=$(cat /sys/class/dmi/id/sys_vendor)
	if [ "$SYS_VENDOR" != "National Instruments" ]; then
		# Is this a VM? Only execute this on NI controllers.
		echo "INFO: sys_vendor is '${SYS_VENDOR}' but test is only valid on NI controllers."
		return 1
	fi
	return 0
}

# Comma-separate a list
function _commasep { local IFS=","; echo "$*"; }

function test_serial_devices () {
	# Different targets have different numbers (and addresses) of serial
	# ports; about the only guarantee we have is that if we have a
	# ttyS0 (or "COM1" in Windows parlance) it will be at 0x3F8; any other
	# serial ports can be whereever, which means we have to maintain a
	# list.
	#
	# Fortunately, product subfamilies tend to not vary too much, so this
	# table doesn't end up being too bad.
	PRODUCT_NAME=$(cat /sys/class/dmi/id/product_name)
	case "$PRODUCT_NAME" in
		"NI cDAQ-913"*|"NI cRIO-903"*|"NI cRIO-904"*)
			EXPECTED=("ttyS0:0x3F8" "ttyS1:0x2F8")
			;;
		"NI cRIO-905"*)
			# Not directly accessable (goes to USB UART adapter)
			EXPECTED=("ttyS0:0x3F8")
			;;
		"NI CVS-145"*)
			EXPECTED=("ttyS0:0x3F8")
			;;
		"NI IC-31"*)
			EXPECTED=("ttyS0:0x3F8")
			;;
		"NI ISC-17"*)
			EXPECTED=()
			;;
		"NI PXIe-8880")
			# No serial ports :(
			EXPECTED=()
			;;
		"NI PXIe-8"*)
			# Other PXIe controllers have one serial port
			EXPECTED=("ttyS0:0x3F8")
			;;
		"NI sbRIO-960"[389])
			EXPECTED=("ttyS0:0x3F8" "ttyS1:0x2F0" "ttyS2:0x300"
				  "ttyS3:0x310" "ttyS4:0x320" "ttyS5:0x330"
				  "ttyS6:0x340")
			;;
		"NI sbRIO-962"[89]|"NI sbRIO-9638")
			EXPECTED=("ttyS0:0x3F8" "ttyS1:0x350" "ttyS2:0x360"
				  "ttyS3:0x2F0" "ttyS4:0x300" "ttyS5:0x310"
				  "ttyS6:0x320" "ttyS7:0x330" "ttyS8:0x340")
			;;
		*)
			echo "test is missing serial port definitions for ${PRODUCT_NAME}"
			ptest_fail
			return 1
			;;
	esac

	# Enumerate /sys/class/tty for serial devices (in sorted order)
	declare -a ACTUAL
	for TTY in $(ls /sys/class/tty | sort -V); do
		# We only want to care about ones that are on the pnp or
		# platform buses (e.g., built into the controller)
		case $(realpath /sys/class/tty/$TTY) in
			/sys/devices/pnp*|/sys/devices/platform*)
				PORT=$(cat /sys/class/tty/$TTY/port)
				ACTUAL+=("$TTY:$PORT")
				;;
		esac
	done

	# Compare the expected list and the actual list. Since we enumerated
	# in sorted order, we do the easy thing and just stringify them, then
	# compare strings.

	EXPECTED_STR=$(_commasep ${EXPECTED[@]})
	ACTUAL_STR=$(_commasep ${ACTUAL[@]})

	if [ "${EXPECTED_STR}" != "${ACTUAL_STR}" ]; then
		echo "expected to find (${EXPECTED_STR}) but got (${ACTUAL_STR})"
		ptest_fail
		return 1
	else
		echo "INFO: found expected serial devices (${ACTUAL_STR})"
		ptest_pass
		return 0
	fi
}

rc=$ptest_rc

if ! _check_machine_arch; then
	ptest_skip
elif ! _check_machine_vendor_ni; then
	ptest_skip
else
	test_serial_devices
fi

ptest_report
exit $rc
