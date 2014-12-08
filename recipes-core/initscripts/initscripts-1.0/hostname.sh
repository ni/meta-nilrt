#!/bin/sh
#
# hostname.sh   Set hostname.
#
# Version:      @(#)hostname.sh  1.10  26-Feb-2001  miquels@cistron.nl
#

# Check if there is a hostname in ni-rt.ini
HOSTNAME=`/usr/local/natinst/bin/nirtcfg --get section=SystemSettings,token=Host_Name`
if [ $? -eq 0 ] && [ -n "$HOSTNAME" ]; then
    # Set the hostname based on ni-rt.ini
    hostname "$HOSTNAME"
else
    # Generate a default hostname based on the serial# or MAC address

    MODEL=`(/sbin/fw_printenv -n DeviceDesc 2> /dev/null || echo cRIO-Linux) | sed "s/^NI //" `

    SERIAL=`/sbin/fw_printenv -n serial# 2>&1`
    # On success "fw_printenv -n serial# 2>&1" puts 0 in "$?"
    if test $? -ne 0
    then
        # If Uboot variable "fw_setenv serial# AABBCCDD" is not set,
        # use the mac address to generate the name of the target.
        echo `ifconfig -a |grep -o "HWaddr [^ ]*" |head -n 1 |\
           sed -r -e "s/HWaddr /NI-$MODEL-/g" -e "s/://g" -e 's/[^a-zA-Z0-9]+/-/g' -e 's/^-//'` | xargs hostname
    else
        # If Uboot variable is set, then use its value to generate
        # the hostname.
        echo "NI-$MODEL-$SERIAL" | sed -r -e 's/[^a-zA-Z0-9]+/-/g' -e 's/^-//' | xargs hostname
    fi

    # Set the newly generated hostname in ni-rt.ini
    /usr/local/natinst/bin/nirtcfg --set section=SystemSettings,token=Host_Name,value="`hostname`"
fi
