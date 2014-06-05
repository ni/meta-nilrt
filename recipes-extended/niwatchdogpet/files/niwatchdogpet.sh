#!/bin/sh
# Copyright 2014 National Instruments.

PETTER=/usr/sbin/niwatchdogpet

# Make sure the petting tool is available.
test -x "$PETTER" || exit 0

# Pet the watchdog.
$PETTER
