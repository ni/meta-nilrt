lvrt_installed_package()
{
	opkg list-installed ni-labview-realtime 2>/dev/null
}

select_lvrt_cgroup_version()
{
	# Linux 4.14 requires cgroups v1.
	case "$(uname -r)" in
		4.14.*) echo 1; return ;;
	esac

	local lvrt_version major_version
	lvrt_version=$(printf '%s\n' "$1" | awk 'NR == 1 {print $3}')
	major_version=${lvrt_version%%.*}

	# LabVIEW RT 2025 and earlier use v1; otherwise default to v2.
	if [ -n "$major_version" ] && [ "$major_version" -le 25 ]; then
		echo 1
	else
		echo 2
	fi
}
