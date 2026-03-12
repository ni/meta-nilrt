#!/bin/sh
sync
echo "Powering off..."
echo o > /proc/sysrq-trigger
