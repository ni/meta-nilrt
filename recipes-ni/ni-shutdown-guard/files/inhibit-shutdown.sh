inhibit_shutdown()
{
	# Re-entry under systemd-inhibit. Do not pass the marker to other tools.
	if [ "${NI_SHUTDOWN_INHIBITED:-}" = "$0" ]; then
		unset NI_SHUTDOWN_INHIBITED
		return
	fi

	export NI_SHUTDOWN_INHIBITED="$0"
	exec systemd-inhibit --what=shutdown --mode=block --who="${0##*/}" \
		--why="NI system maintenance is in progress" -- "$0" "$@"
}
