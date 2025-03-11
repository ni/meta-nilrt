#!/bin/sh

. /etc/default/lvrt-cgroup
. /usr/share/ni-cgroups/lvrt-cgroup-version.sh

identify_lvrt_cgroup_version()
{
	# Return early if a version is already set
	[ -z "$LVRT_CGROUP_VERSION" ] || return 0

	# If lvrt is not installed set version to 0, cgroups not used
	local lvrt_installed
	lvrt_installed=$(lvrt_installed_package)
	if [ -z "$lvrt_installed" ]; then
		LVRT_CGROUP_VERSION=0
		return 1
	fi

	LVRT_CGROUP_VERSION=$(select_lvrt_cgroup_version "$lvrt_installed")
}
