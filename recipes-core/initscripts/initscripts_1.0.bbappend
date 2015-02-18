
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}-${PV}:"

SRC_URI += "file://urandom.default"

do_install_append() {
	# install urandom defaults file
	install -d ${D}${sysconfdir}/default
	install -m 0644 ${WORKDIR}/urandom.default ${D}${sysconfdir}/default/urandom

	# re-assign urandom runlevel links
	update-rc.d -r ${D} -f urandom remove
	update-rc.d -r ${D} urandom start 41 S . stop 1 0 6 .

	# re-assign populate-volatile.sh runlevel links
	update-rc.d -r ${D} -f populate-volatile.sh remove
	update-rc.d -r ${D} populate-volatile.sh start 3 S .

	# re-assign mountall.sh runlevel links
	update-rc.d -r ${D} -f mountall.sh remove
	update-rc.d -r ${D} mountall.sh start 2 S .

	# remove the scripts from the rc folders, but keep them around
	update-rc.d -f -r ${D} banner.sh remove
	update-rc.d -f -r ${D} checkroot.sh remove
	update-rc.d -f -r ${D} mountnfs.sh remove
	update-rc.d -f -r ${D} umountnfs.sh remove
	update-rc.d -f -r ${D} read-only-rootfs-hook.sh remove
}
