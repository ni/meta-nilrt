#!/bin/sh
sync
echo "Rebooting..."
echo b > /proc/sysrq-trigger
