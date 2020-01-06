if [ -f /etc/natinst/safemode ] || /sbin/runlevel | egrep -q ' 4$' ; then
	PS1="(safemode) $PS1"
fi
