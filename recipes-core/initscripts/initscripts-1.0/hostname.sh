#!/bin/sh
#
# hostname.sh   Set hostname.
#
# Version:      @(#)hostname.sh  1.10  26-Feb-2001  miquels@cistron.nl
#

# Check if there is a hostname in ni-rt.ini
HOSTNAME=$(/usr/local/natinst/bin/nirtcfg --get section=SystemSettings,token=Host_Name)
if [ $? -eq 0 ] && [ -n "$HOSTNAME" ]; then
    # Set the hostname based on ni-rt.ini
    hostname "$HOSTNAME"
else
    # Generate a default hostname based on the serial# or MAC address
    MODEL=$((/sbin/fw_printenv -n DeviceDesc 2> /dev/null || echo PXI-Unknown) | sed "s/^NI //;s/ Embedded Controller//")

    # Only use the last 8 characters of the serial number (after spaces are stripped)
    SERIAL=$(/sbin/fw_printenv -n serial# 2>&1 | tr -d "\n " | tail -c 8)

    echo "NI-$MODEL-$SERIAL" | sed -r -e 's/[^a-zA-Z0-9]+/-/g' -e 's/^-//' | xargs hostname

    # Set the newly generated hostname in ni-rt.ini
    /usr/local/natinst/bin/nirtcfg --set section=SystemSettings,token=Host_Name,value="$(hostname)"
fi
