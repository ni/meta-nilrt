#!/bin/bash

# Get all of the Architecture listings opkg will use

ARCH_AGNOSTIC_REGEX="(all|any|noarch)"
KERNEL_ARCH="cortexa9-vfpv3-kernel"

ALL_ARCH_LIST=$(grep -h ^arch /etc/opkg/*.conf | sort -k3 -n)

# Filter-out the arch-agnostic listings

ARCH_SPECIFIC_LIST=$(echo "${ALL_ARCH_LIST}" | grep -vE "${ARCH_AGNOSTIC_REGEX}")

# Ensure that the kernel listing is at the top of the arch-specific list

echo "${ARCH_SPECIFIC_LIST}" | head -n 1 | grep -q ${KERNEL_ARCH}
