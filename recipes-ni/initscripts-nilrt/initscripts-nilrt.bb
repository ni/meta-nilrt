SUMMARY = "SysV nilrt init scripts"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

inherit ptest

DEPENDS += "shadow-native pseudo-native update-rc.d-native niacctbase"

SRC_URI = "\
	file://cleanvarcache \
	file://iso3166-translation.txt \
	file://mountconfig \
	file://niconfiguretracefs \
	file://nicheckbiosconfig \
	file://nicleanefivars \
	file://nicleanstalelinks \
	file://nidisablecstates \
	file://nipopulateconfigdir \
	file://nisetbootcount \
	file://nisetcommitratio \
	file://nisetreboottype \
	file://nisetupkernelconfig \
	file://niusbgadget \
	file://populateconfig \
	file://run-ptest \
	file://test-nisetcommitratio-common.sh \
	file://test-nisetcommitratio-system \
	file://test-nisetcommitratio-unit \
	file://test-safemode-runlevel-init \
	file://wirelesssetdomain \
"

SRC_URI:append:xilinx-zynq = "\
	file://firewall \
	file://mountutils \
"

S = "${UNPACKDIR}"

do_install () {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${S}/cleanvarcache         ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/mountconfig           ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/niconfiguretracefs    ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nicheckbiosconfig     ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nicleanefivars        ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nicleanstalelinks     ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nidisablecstates      ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nipopulateconfigdir   ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nisetcommitratio      ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nisetreboottype       ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nisetupkernelconfig   ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/populateconfig        ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/wirelesssetdomain     ${D}${sysconfdir}/init.d

	update-rc.d -r ${D} cleanvarcache         start 38 0 6 S .
	update-rc.d -r ${D} mountconfig           start 35 S .
	update-rc.d -r ${D} niconfiguretracefs    start 82 S .
	update-rc.d -r ${D} nicheckbiosconfig     start 99 4 5 .
	update-rc.d -r ${D} nicleanefivars        start 10 S .
	update-rc.d -r ${D} nicleanstalelinks     start 5  S .
	update-rc.d -r ${D} nidisablecstates      start 2 3 4 5 S .
	update-rc.d -r ${D} nipopulateconfigdir   start 35 S .
	update-rc.d -r ${D} nisetcommitratio      start 99 S .
	update-rc.d -r ${D} nisetreboottype       stop  55 6 .
	update-rc.d -r ${D} nisetupkernelconfig   start 3  5 .
	update-rc.d -r ${D} populateconfig        start 35 S . start 30 0 6 .
	update-rc.d -r ${D} wirelesssetdomain     start 36 S .

	install -d ${D}${sysconfdir}/natinst
	install -m 0644 ${S}/iso3166-translation.txt ${D}${sysconfdir}/natinst
}

do_install:append:xilinx-zynq () {
	install -m 0755 ${WORKDIR}/mountutils            ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/nisetbootcount        ${D}${sysconfdir}/init.d
	install -m 0750 ${WORKDIR}/niusbgadget           ${D}${sysconfdir}/init.d

	update-rc.d -r ${D} nisetbootcount        start 40 S .
	update-rc.d -r ${D} niusbgadget           start 0  5 . stop 81 0 6 .

	# Legacy static iptables firewall, retained for non-x64 (arm) targets until
	# firewalld is supported there (requires a kernel >= 4.18 for the nftables
	# inet backend). x64 uses firewalld instead (see packagegroup-ni-runmode).
	install -m 0755 ${S}/firewall              ${D}${sysconfdir}/init.d
	# Substitute configfs paths
	sed -i 's|^IPTABLES_CONF=.*$|IPTABLES_CONF=/etc/natinst/share/iptables.conf|g' ${D}${sysconfdir}/init.d/firewall
	sed -i 's|^IP6TABLES_CONF=.*$|IP6TABLES_CONF=/etc/natinst/share/ip6tables.conf|g' ${D}${sysconfdir}/init.d/firewall
	# sanity check: break build if new _CONF vars exist which aren't substituted above
	! egrep '^[a-zA-Z0-9]*_CONF=.*$' ${D}${sysconfdir}/init.d/firewall | egrep -v '^(IPTABLES_CONF)|(IP6TABLES_CONF)=.*$'

	update-rc.d -r ${D} firewall              start 39 S .
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
	cp ${S}/test-nisetcommitratio-* ${D}${PTEST_PATH}/
	cp ${S}/test-safemode-runlevel-init ${D}${PTEST_PATH}/
}

# /etc/init.d/populateconfig invokes wpa-supplicant.ipk scripts.
RDEPENDS:${PN} += "\
	bash \
	niacctbase \
	update-rc.d \
	wpa-supplicant \
"
RDEPENDS:${PN}-ptest += "bash"
