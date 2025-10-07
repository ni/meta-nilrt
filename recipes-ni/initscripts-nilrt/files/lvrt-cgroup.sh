#!/bin/sh

. /etc/default/lvrt-cgroup

identify_lvrt_cgroup_version()
{
	# Return early if a version is already set
	[ -z "$LVRT_CGROUP_VERSION" ] || return 0

	# If LabVIEW RT is not installed, default to cgroups v2
	lvrt_installed=$(opkg list-installed ni-labview-realtime)
	if [ -z "$lvrt_installed" ]; then
		LVRT_CGROUP_VERSION=2
		return 0
	fi

	# Extract installed LabVIEW RT version
	lvrt_version=$(awk '{print $3}' <<<"$lvrt_installed")
	
	# Get major version
	major_version=${lvrt_version%%.*}
	if [ "$major_version" -le 23 ]; then
		LVRT_CGROUP_VERSION=1
	else
		LVRT_CGROUP_VERSION=2
	fi
	return 0
}
