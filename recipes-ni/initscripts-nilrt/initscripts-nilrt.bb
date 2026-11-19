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
	install -m 0755 ${UNPACKDIR}/mountutils            ${D}${sysconfdir}/init.d
	install -m 0755 ${UNPACKDIR}/nisetbootcount        ${D}${sysconfdir}/init.d
	install -m 0750 ${UNPACKDIR}/niusbgadget           ${D}${sysconfdir}/init.d

	update-rc.d -r ${D} nisetbootcount        start 40 S .
	update-rc.d -r ${D} niusbgadget           start 0  5 . stop 81 0 6 .
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
	class="`/sbin/fw_printenv -n TargetClass 2>/dev/null || true`"

	# Detect the target and select the net device naming scheme.
	# For more information, see systemd.net-naming-scheme(7).
	biosdevname=

	# net.ifnames=0 pre-empts all other logic. Note: net.ifnames=1 is not checked.
	# This matches 80-net-name-slot.rules.
	[ -z "$biosdevname" ] && grep -Fq 'net.ifnames=0' /proc/cmdline && biosdevname=0

	# On non-NI hardware, always use modern naming. Note: At present, NI ARM targets
	# do not set NITarget.
	[ -z "$biosdevname" ] && [ `/sbin/fw_printenv -n NITarget 2>/dev/null` = false ] && biosdevname=1

	# On PXI Linux RT and USRP, always use modern naming.
	[ -z "$biosdevname" ] && case "$class" in
		PXI|"USRP Stand-Alone Devices") biosdevname=1 ;;
	esac

	# DeviceDesc include the model number, prefixed by a dash. If it is at least
	# five digits long, always use modern naming.
	[ -z "$biosdevname" ] && case `/sbin/fw_printenv -n DeviceDesc 2>/dev/null` in
		*-[0-9][0-9][0-9][0-9][0-9]*) biosdevname=1 ;;
	esac

	# /lib/udev/rules.d/80-net-name-slot.rules effects modern network device naming.
	# To disable it, override with an empty file in /etc/udev/rules.d.
	if (( biosdevname )); then
		rm -f /etc/udev/rules.d/80-net-name-slot.rules
	else
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

RDEPENDS:${PN} += "\
	bash \
	niacctbase \
	update-rc.d \
"
RDEPENDS:${PN}-ptest += "bash"
