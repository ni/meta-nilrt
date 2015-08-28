if [ -z "$LANG" ]; then
	[ -r /etc/locale.conf ] && . /etc/locale.conf
	LANG=${LANG:-en_US}
	export LANG
fi
