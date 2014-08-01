
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}-${PV}:"

do_install_append() {
	# re-assign urandom runlevel links
	update-rc.d -r ${D} -f urandom remove
	update-rc.d -r ${D} urandom start 41 S . stop 1 0 6 .

	# re-assign populate-volatile.sh runlevel links
	update-rc.d -r ${D} -f populate-volatile.sh remove
	update-rc.d -r ${D} populate-volatile.sh start 3 S .

	# re-assign mountall.sh runlevel links
	update-rc.d -r ${D} -f mountall.sh remove
	update-rc.d -r ${D} mountall.sh start 2 S .

	# remove unnecessary startup scripts
	rm ${D}${sysconfdir}/init.d/banner.sh
	rm ${D}${sysconfdir}/init.d/checkroot.sh
	rm ${D}${sysconfdir}/init.d/mountnfs.sh
	rm ${D}${sysconfdir}/init.d/read-only-rootfs-hook.sh
	update-rc.d -r ${D} banner.sh remove
	update-rc.d -r ${D} checkroot.sh remove
	update-rc.d -r ${D} mountnfs.sh remove
	update-rc.d -r ${D} read-only-rootfs-hook.sh remove
}
