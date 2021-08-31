SUMMARY = "SysV nilrt init scripts"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

inherit ptest

DEPENDS += "shadow-native pseudo-native update-rc.d-native niacctbase"

RDEPENDS_${PN} += "bash niacctbase update-rc.d"
RDEPENDS_${PN}-ptest += "bash"

DEPENDS_append_x64 = " nilrtdiskcrypt "
RDEPENDS_${PN}_append_x64 = " nilrtdiskcrypt "

SRC_URI = "\
           file://firewall \
           file://mountconfig \
           file://mountdebugfs \
           file://nicleanefivars \
           file://nicleanstalelinks \
           file://nicreatecpusets \
           file://nicreatecpuacctgroups \
           file://nisetupkernelconfig \
           file://nisetcommitratio \
           file://test-nisetcommitratio-common.sh \
           file://test-nisetcommitratio-system \
           file://test-nisetcommitratio-unit \
           file://test-safemode-runlevel-init \
           file://wirelesssetdomain \
           file://iso3166-translation.txt \
           file://nipopulateconfigdir \
           file://populateconfig \
           file://run-ptest \
           file://cleanvarcache \
           file://nidisablecstates \
           file://nisetembeddeduixml \
           file://nicheckbiosconfig \
           file://niopendisks \
           file://niclosedisks \
           file://nisetreboottype \
           file://test-niopendisks-init \
           file://test-niclosedisks-init \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/

	install -m 0755 ${WORKDIR}/firewall              ${D}${sysconfdir}/init.d

	# this logic is only for nilrt and nilrt-xfce, not for nilrt-nxg
	if ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
		# Substitute configfs paths
		sed -i 's|^IPTABLES_CONF=.*$|IPTABLES_CONF=/etc/natinst/share/iptables.conf|g' ${D}${sysconfdir}/init.d/firewall
		sed -i 's|^IP6TABLES_CONF=.*$|IP6TABLES_CONF=/etc/natinst/share/ip6tables.conf|g' ${D}${sysconfdir}/init.d/firewall

		# sanity check: break build if new _CONF vars exist which aren't substituted above
		! egrep '^[a-zA-Z0-9]*_CONF=.*$' ${D}${sysconfdir}/init.d/firewall | egrep -v '^(IPTABLES_CONF)|(IP6TABLES_CONF)=.*$'
	fi

	install -m 0755 ${WORKDIR}/mountconfig           ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/mountdebugfs          ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetembeddeduixml    ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicleanstalelinks     ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicreatecpusets       ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicreatecpuacctgroups ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nipopulateconfigdir   ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/populateconfig        ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetupkernelconfig   ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetcommitratio      ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetreboottype       ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/wirelesssetdomain     ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/cleanvarcache         ${D}${sysconfdir}/init.d

	install -d ${D}${sysconfdir}/natinst
	install -m 0644 ${WORKDIR}/iso3166-translation.txt ${D}${sysconfdir}/natinst

	update-rc.d -r ${D} nicreatecpusets       start 1  4 5 .
	update-rc.d -r ${D} nicreatecpuacctgroups start 2  4 5 .
	update-rc.d -r ${D} nisetupkernelconfig   start 3  5 .
	update-rc.d -r ${D} nicleanstalelinks     start 5  S .
	update-rc.d -r ${D} nisetembeddeduixml    start 20 5 .
	update-rc.d -r ${D} nipopulateconfigdir   start 36 S .
	update-rc.d -r ${D} mountconfig           start 36 S .
	update-rc.d -r ${D} populateconfig        start 36 S . start 30 0 6 .
	update-rc.d -r ${D} wirelesssetdomain     start 36 S .
	update-rc.d -r ${D} cleanvarcache         start 38 0 6 S .
	update-rc.d -r ${D} firewall              start 39 S .
	update-rc.d -r ${D} mountdebugfs          start 82 S .
	update-rc.d -r ${D} nisetcommitratio      start 99 S .

	update-rc.d -r ${D} nisetreboottype       stop  55 6 .
}

pkg_postinst_ontarget_${PN} () {
	# Make sure /boot is mounted so that fw_printenv is usable
	if /sbin/fw_printenv TargetClass > /dev/null 2>&1; then
		mountstate=1
	else
		mountstate=0
		mount /boot || mountstate=1
	fi

	# Get target class, may be empty-string on VMs
	class="`/sbin/fw_printenv -n TargetClass || true`"

	# Use persistent names on PXI, not on any other targets
	if [ "$class" != "PXI" -a "$class" != "USRP Stand-Alone Devices" ]; then
		touch /etc/udev/rules.d/80-net-name-slot.rules

		# Since the network is already brought up on the first boot, reload the network to get the new rules
		if ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'true', 'false', d)}; then

			NXG_ETHERNET_DRIVERS="igb e1000 virtio-pci"
			active_drivers=""
			for driver in $NXG_ETHERNET_DRIVERS; do
				if `lsmod | grep -q "$driver"`; then
					active_drivers="$active_drivers $driver"
				fi
			done

			/etc/init.d/connman stop
			/etc/init.d/udev stop
			for mod in $active_drivers; do
				modprobe -r "$mod"
				modprobe "$mod"
			done
			/etc/init.d/udev start
			/etc/init.d/connman start
		fi
	fi


	# Enable core dumps on PXI, not on any other targets
	[ "$class" = "PXI" ] && echo "* soft core unlimited" > /etc/security/limits.d/allow-core-dumps.conf

	# Restore the original state of /boot
	[ $mountstate == 0 ] && umount /boot || true
}

do_install_append_x64 () {
	install -m 0755   ${WORKDIR}/nidisablecstates      ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} nidisablecstates start 2 3 4 5 S .
	install -m 0755   ${WORKDIR}/nicheckbiosconfig      ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} nicheckbiosconfig start 99 4 5 .

	install -m 0755   ${WORKDIR}/niopendisks   ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} niopendisks start 00 S .

	install -m 0755   ${WORKDIR}/niclosedisks  ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} niclosedisks start 41 0 . start 41 6 .

	install -m 0755   ${WORKDIR}/nicleanefivars  ${D}${sysconfdir}/init.d
	update-rc.d -r ${D} nicleanefivars start 10 S .
}

do_install_ptest () {
	cp ${WORKDIR}/test-nisetcommitratio-* ${D}${PTEST_PATH}/
	cp ${WORKDIR}/test-safemode-runlevel-init ${D}${PTEST_PATH}/
}

do_install_ptest_append_x64 () {
	cp ${WORKDIR}/test-niopendisks-init  ${D}${PTEST_PATH}/
	cp ${WORKDIR}/test-niclosedisks-init  ${D}${PTEST_PATH}/
}
