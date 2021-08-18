INSTALL_TIMEZONE_FILE = "0"

pkg_postinst_${PN}_append() {
	# create symlink to shared default timezone settings (UTC)
	ln -sf /etc/natinst/share/localtime $D/etc/localtime
}
