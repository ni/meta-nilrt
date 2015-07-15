do_install_append() {
	# replace localtime symlink to shared default timezone settings (London, GMT) 
	# between runmode & safemode
	if [ -e ${D}${sysconfdir}/localtime ]; then
		rm ${D}${sysconfdir}/localtime
		ln -s ${sysconfdir}/natinst/share/localtime ${D}${sysconfdir}/localtime
	fi
}
