do_install:append() {
	sed -i 's/^# \(%sudo	ALL=(ALL:ALL) ALL\)$/\1/' ${D}${sysconfdir}/sudoers
}
