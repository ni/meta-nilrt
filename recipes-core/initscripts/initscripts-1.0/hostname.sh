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
    # Persist hostname in /etc/hostname file
    hostname > /etc/hostname
else
    # Generates a default hostname based on firmware DeviceDesc and serial# or MAC address

    # Grab model number from firmware
    # Fixup Embedded Controller name
    # Strip whitespace and only use the last 20 characters
    MODEL=$((/sbin/fw_printenv -n DeviceDesc 2>/dev/null || echo "Unknown") | sed "s/^NI //;s/ Embedded Controller//" | tr -d "\n " | tail -c 20)

    # Grab serial number from firmware
    # Strip whitespace and only use the last 20 characters
    SERIAL=$((/sbin/fw_printenv -n serial# 2>/dev/null) | tr -d "\n " | tail -c 20)

    if [ -z "$SERIAL" ]; then
        # Fallback to MAC address from network driver
        # Prefer eth0
        # Filer out lo adapter's MAC (all zeros)
        SERIAL=$((cat /sys/class/net/eth0/address /sys/class/net/*/address 2>/dev/null) | grep -v '^00:00:00:00:00:00$' | head -1 | tr -d "\n: " | tail -c 20)
    fi

    if [ -z "$SERIAL" ]; then
        # Unknown serial number
        SERIAL="Unknown"
    fi

    # Set new hostname
    echo "NI-$MODEL-$SERIAL" | sed -r -e 's/[^a-zA-Z0-9]+/-/g' -e 's/^-//' | xargs hostname

    # Persist new hostname in /etc/hostname file
    hostname > /etc/hostname
    # Persist new hostname in ni-rt.ini
    /usr/local/natinst/bin/nirtcfg --set section=SystemSettings,token=Host_Name,value="$(hostname)"

    # Print new hostname to screen
    echo -n "New system hostname: "
    hostname
fi
