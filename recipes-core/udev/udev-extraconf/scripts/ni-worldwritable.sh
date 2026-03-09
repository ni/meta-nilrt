#!/bin/sh
# Echo udev environment keys for IMPORT{program}
# Reads /etc/natinst/share/ni-rt.ini and outputs worldwritable.enabled=<true|false>

enable=$(/usr/local/natinst/bin/nirtcfg --get section=SystemSettings,token=worldwritable.enabled,value="false" | tr "[:upper:]" "[:lower:]")

case "$enable" in
     true) NI_WORLDWRITABLE_VAL=1 ;;
    *)     NI_WORLDWRITABLE_VAL=0 ;;
esac

# udev IMPORT{program} expects KEY=VALUE pairs on stdout
echo "NI_WORLDWRITABLE=$NI_WORLDWRITABLE_VAL"
exit 0
