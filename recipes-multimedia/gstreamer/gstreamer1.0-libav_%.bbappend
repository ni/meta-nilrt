
inherit nilrt-permit-commercial

do_sanity_check_commercial() {
	# gstreamer components are distributed under either GPL or LGPL licenses,
	# which are generally compatible with NILRT's distribution strategy.
	exit 0
}
