if [ -z "$LANG" ]; then
	[ -r /etc/locale.conf ] && . /etc/locale.conf
	LANG=${LANG:-en_US.UTF-8}
	export LANG
fi
