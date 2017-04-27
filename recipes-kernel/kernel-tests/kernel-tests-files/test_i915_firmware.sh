#!/bin/bash

FAILED=0

ARCH=`uname -m`
if [ "$ARCH" = "armv7l" ] ; then
	echo "SKIP: `basename $0`"
	echo "i915 firmware is not installed on ARM targets"
	exit 1
fi

if [ ! -d /lib/firmware/i915 ] ; then
	FAILED=1
fi

SYMS=( "bxt_dmc_ver1.bin" "kbl_dmc_ver1.bin" "skl_dmc_ver1.bin" "skl_guc_ver6.bin" )

for s in "${SYMS[@]}" ; do
	if [ ! -h /lib/firmware/i915/$s ] ; then
		FAILED=1
	fi
done

FILES=( "bxt_dmc_ver1_07.bin" "kbl_dmc_ver1_01.bin" "skl_dmc_ver1_26.bin" "skl_guc_ver6_1.bin" )

for f in "${FILES[@]}" ; do
	if [ ! -f /lib/firmware/i915/$f ] ; then
		FAILED=1
	fi
done

MOD=$(lsmod |grep "^i915 " |awk '{print $3}')
if [ $MOD -eq 0 ] ; then
	FAILED=1
fi

if [ $FAILED -eq 0 ] ; then
	echo "PASS: `basename $0`"
else
	echo "FAIL: `basename $0`"
fi

