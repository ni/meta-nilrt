#!/bin/bash

#test if all up net interfaces have a link local route

FAIL_TEST=0

ifconfig -a | sed 's/[ \t].*//;/^$/d' | while read if_name
do
	if_up=$(cat /sys/class/net/$if_name/operstate)
	if [ "$if_up" = "up" ]; then
		 #if up, check if interface has a link local route
		ll_route=$(route | grep $if_name | grep 255.255.0.0)
		if [ "x$ll_route" = "x" ]; then
			FAIL_TEST=1
		fi
	fi
done

if [ $FAIL_TEST -eq 1 ]; then
    echo "FAIL: kernel doesn't have link local routes for up interfaces"
fi

echo "PASS: kernel has link local routes for up interfaces"
