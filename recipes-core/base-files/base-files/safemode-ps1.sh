if [ -f /etc/natinst/safemode ] || /sbin/runlevel | grep -Eq ' 4$' ; then
	PS1="(safemode) $PS1"
fi
