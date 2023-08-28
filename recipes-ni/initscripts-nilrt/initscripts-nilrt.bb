SUMMARY = "SysV nilrt init scripts"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

inherit ptest

DEPENDS += "shadow-native pseudo-native update-rc.d-native niacctbase"

SRC_URI = "\
	file://cleanvarcache \
	file://firewall \
	file://iso3166-translation.txt \
	file://lvrt-cgroup \
	file://lvrt-cgroup.sh \
	file://mountconfig \
	file://mountdebugfs \
	file://nicheckbiosconfig \
	file://nicleanefivars \
	file://nicleanstalelinks \
	file://nicreatecpuacctgroups \
	file://nicreatecpusets \
	file://nidisablecstates \
	file://nipopulateconfigdir \
	file://nisetcommitratio \
	file://nisetreboottype \
	file://nisetupkernelconfig \
	file://populateconfig \
	file://run-ptest \
	file://test-nisetcommitratio-common.sh \
	file://test-nisetcommitratio-system \
	file://test-nisetcommitratio-unit \
	file://test-safemode-runlevel-init \
	file://wirelesssetdomain \
"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/cleanvarcache         ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/mountconfig           ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/mountdebugfs          ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicheckbiosconfig     ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicleanefivars        ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicleanstalelinks     ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicreatecpuacctgroups ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nicreatecpusets       ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nidisablecstates      ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nipopulateconfigdir   ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetcommitratio      ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetreboottype       ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetupkernelconfig   ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/populateconfig        ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/wirelesssetdomain     ${D}${sysconfdir}/init.d

	install -m 0755 ${WORKDIR}/firewall              ${D}${sysconfdir}/init.d
	# Substitute configfs paths
	sed -i 's|^IPTABLES_CONF=.*$|IPTABLES_CONF=/etc/natinst/share/iptables.conf|g' ${D}${sysconfdir}/init.d/firewall
	sed -i 's|^IP6TABLES_CONF=.*$|IP6TABLES_CONF=/etc/natinst/share/ip6tables.conf|g' ${D}${sysconfdir}/init.d/firewall
	# sanity check: break build if new _CONF vars exist which aren't substituted above
	! egrep '^[a-zA-Z0-9]*_CONF=.*$' ${D}${sysconfdir}/init.d/firewall | egrep -v '^(IPTABLES_CONF)|(IP6TABLES_CONF)=.*$'

	update-rc.d -r ${D} cleanvarcache         start 38 0 6 S .
	update-rc.d -r ${D} firewall              start 39 S .
	update-rc.d -r ${D} mountconfig           start 35 S .
	update-rc.d -r ${D} mountdebugfs          start 82 S .
	update-rc.d -r ${D} nicheckbiosconfig     start 99 4 5 .
	update-rc.d -r ${D} nicleanefivars        start 10 S .
	update-rc.d -r ${D} nicleanstalelinks     start 5  S .
	update-rc.d -r ${D} nicreatecpuacctgroups start 2  4 5 .
	update-rc.d -r ${D} nicreatecpusets       start 1  4 5 .
	update-rc.d -r ${D} nidisablecstates      start 2 3 4 5 S .
	update-rc.d -r ${D} nipopulateconfigdir   start 35 S .
	update-rc.d -r ${D} nisetcommitratio      start 99 S .
	update-rc.d -r ${D} nisetreboottype       stop  55 6 .
	update-rc.d -r ${D} nisetupkernelconfig   start 3  5 .
	update-rc.d -r ${D} populateconfig        start 35 S . start 30 0 6 .
	update-rc.d -r ${D} wirelesssetdomain     start 36 S .

	install -d ${D}${sysconfdir}/natinst
	install -m 0644 ${WORKDIR}/iso3166-translation.txt ${D}${sysconfdir}/natinst

	install -d ${D}${sysconfdir}/default
	install -m 0644 lvrt-cgroup ${D}${sysconfdir}/default/lvrt-cgroup

	install -d ${D}${datadir}/${BPN}
	install -m 0755 lvrt-cgroup.sh ${D}${datadir}/${BPN}/lvrt-cgroup.sh
}

pkg_postinst_ontarget:${PN} () {
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
	fi

	# Enable core dumps on PXI, not on any other targets
	[ "$class" = "PXI" ] && echo "* soft core unlimited" > /etc/security/limits.d/allow-core-dumps.conf

	# Restore the original state of /boot
	[ $mountstate == 0 ] && umount /boot || true
}

do_install_ptest () {
	cp ${WORKDIR}/test-nisetcommitratio-* ${D}${PTEST_PATH}/
	cp ${WORKDIR}/test-safemode-runlevel-init ${D}${PTEST_PATH}/
}

# /etc/init.d/populateconfig invokes wpa-supplicant.ipk scripts.
RDEPENDS:${PN} += "\
	bash \
	niacctbase \
	update-rc.d \
	wpa-supplicant \
"
RDEPENDS:${PN}-ptest += "bash"
