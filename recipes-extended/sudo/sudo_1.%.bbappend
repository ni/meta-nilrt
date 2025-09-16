do_install:append() {
	sed -i 's/^#\s\(%sudo\sALL=(ALL:ALL)\sALL\)$/\1/' ${D}${sysconfdir}/sudoers
}
