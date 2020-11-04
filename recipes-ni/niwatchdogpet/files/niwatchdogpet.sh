#!/bin/sh
# Copyright 2014 National Instruments.

PETTER=/usr/sbin/niwatchdogpet

class="`/sbin/fw_printenv -n TargetClass`"

# PXI targets use the built-in watchdog rather than CPLD
[ "$class" = "PXI" ] && exit 0 

# Make sure the petting tool is available.
test -x "$PETTER" || exit 0

# Pet the watchdog.
$PETTER
