#!/bin/sh

. /etc/default/lvrt-cgroup

identify_lvrt_cgroup_version()
{
	# Return early if a version is already set
	[ -z "$LVRT_CGROUP_VERSION" ] || return 0

	# If lvrt is not installed set version to 0, cgroups not used
	lvrt_installed=$(opkg list-installed ni-labview-realtime)
	if [ -z "$lvrt_installed" ]; then
		LVRT_CGROUP_VERSION=0
		return 1
	fi

	# By default assume LabVIEW RT uses cgroups-v1 (current situation). This
	# will be amended when LV cgroup-v2 upgrade is implemented.
	LVRT_CGROUP_VERSION=1
	return 0
}
