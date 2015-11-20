#!/bin/bash

NEW_IN=0
#exit if only one monitor is present
if [ `xfconf-query -c displays -l | wc -l` != 0 ]; then
	exit 0
fi
if [ "1" == `xrandr | grep ' connected' | wc -l` ]; then
	exit 0
fi
# Obtaining information of monitors and right touch
echo "Obtaining resolution of monitors..."
read DP1 D1 DP2 D2 <<< $(xrandr | awk -F '[[:space:]+]' \
	'/ connected/ { if ($3 != "primary") print $4, $1; if ($3 == "primary") print $5, $1}')

if [ "$DP1" == 0 ]; then
	read DISP_L DISP_R <<< $(xrandr | awk -F '[[:space:]+]' \
		'/ connected/ { if ($3 != "primary") print $3; if ($3 == "primary") print $4 }')
	DP_L="$D1"
	DP_R="$D2"
else
	read DISP_R DISP_L <<< $(xrandr | awk -F '[[:space:]+]' \
		'/ connected/ { if ($3 != "primary") print $3; if ($3 == "primary") print $4 }')
	DP_L="$D2"
	DP_R="$D1"
fi
echo $DP1 $D1 $DP2 $D2
echo "Enabling monitors..."
xrandr --auto --output $DP_L --primary --output $DP_R --right-of $DP_L
