if [ -f /etc/natinst/safemode ] || {
	[ ! -d /run/systemd/system ] && /sbin/runlevel | grep -Eq ' 4$'
}; then
	PS1="(safemode) $PS1"
fi
