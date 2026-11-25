SUMMARY = "SystemD nilrt Services"
DESCRIPTION = "nilrt distro-specific services to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "shadow-native pseudo-native niacctbase"

# Share the UBI mount helper with SysVinit; callers source it from /etc/init.d.
FILESEXTRAPATHS:append:xilinx-zynq := ":${THISDIR}/../initscripts-nilrt/files"
PACKAGE_ARCH:xilinx-zynq = "${MACHINE_ARCH}"

SRC_URI = "\
	file://cleanvarcache.service \
	file://firewall.service \
	file://firewall \
	file://iso3166-translation.txt \
	file://lvrt-cgroup \
	file://lvrt-cgroup.sh \
	file://mountconfig.service \
	file://mountconfig \
	file://nicheckbiosconfig.service \
	file://nicheckbiosconfig \
	file://nicleanefivars.service \
	file://nicleanstalelinks.service \
	file://nicreatecpuacctgroups.service \
	file://nicreatecpuacctgroups \
	file://nicreatecpusets.service \
	file://nicreatecpusets \
	file://niconfiguretracefs.service \
	file://niconfiguretracefs \
	file://nidisablecstates.service \
	file://nisetcommitratio.service \
	file://nisetcommitratio \
	file://nisetreboottype.service \
	file://nisetreboottype \
	file://nisetupkernelconfig.service \
	file://nisetupkernelconfig \
	file://nipopulateconfigdir.service \
	file://populateconfig.service \
	file://populateconfig \
	file://wirelesssetdomain.service \
	file://wirelesssetdomain \
"

# nisetbootcount and niusbgadget are shared with the SysVinit recipe, found via FILESEXTRAPATHS above.
SRC_URI:append:xilinx-zynq = " \
	file://mountutils \
	file://nisetbootcount \
	file://nisetbootcount.service \
	file://niusbgadget \
	file://niusbgadget.service \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
SYSTEMD_SERVICE:${PN} = "\
	cleanvarcache.service \
	firewall.service \
	mountconfig.service \
	nicheckbiosconfig.service \
	nicleanefivars.service \
	nicleanstalelinks.service \
	nicreatecpuacctgroups.service \
	nicreatecpusets.service \
	niconfiguretracefs.service \
	nidisablecstates.service \
	nisetcommitratio.service \
	nisetreboottype.service \
	nisetupkernelconfig.service \
	nipopulateconfigdir.service \
	populateconfig.service \
	wirelesssetdomain.service \
"

S = "${UNPACKDIR}"

do_install () {
	install -d ${D}${systemd_unitdir}/system
	install -d ${D}${libdir}/systemd/scripts

	install -m 0644 ${S}/cleanvarcache.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/mountconfig.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/mountconfig ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nicheckbiosconfig.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nicheckbiosconfig ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nicleanefivars.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/nicleanstalelinks.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/nicreatecpuacctgroups.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nicreatecpuacctgroups ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nicreatecpusets.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nicreatecpusets ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/niconfiguretracefs.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/niconfiguretracefs ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nidisablecstates.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/nisetcommitratio.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nisetcommitratio ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nisetreboottype.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nisetreboottype ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nisetupkernelconfig.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/nisetupkernelconfig ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/nipopulateconfigdir.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/populateconfig.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/populateconfig ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/wirelesssetdomain.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/wirelesssetdomain ${D}${libdir}/systemd/scripts

	install -m 0644 ${S}/firewall.service ${D}${systemd_unitdir}/system
	install -m 0755 ${S}/firewall ${D}${libdir}/systemd/scripts
	# Substitute configfs paths
	sed -i 's|^IPTABLES_CONF=.*$|IPTABLES_CONF=/etc/natinst/share/iptables.conf|g' ${D}${libdir}/systemd/scripts/firewall
	sed -i 's|^IP6TABLES_CONF=.*$|IP6TABLES_CONF=/etc/natinst/share/ip6tables.conf|g' ${D}${libdir}/systemd/scripts/firewall
	# sanity check: break build if new _CONF vars exist which aren't substituted above
	! egrep '^[a-zA-Z0-9]*_CONF=.*$' ${D}${libdir}/systemd/scripts/firewall | egrep -v '^(IPTABLES_CONF)|(IP6TABLES_CONF)=.*$'

	install -d ${D}${sysconfdir}/natinst
	install -m 0644 ${S}/iso3166-translation.txt ${D}${sysconfdir}/natinst

	install -d ${D}${sysconfdir}/default
	install -m 0644 ${S}/lvrt-cgroup ${D}${sysconfdir}/default/lvrt-cgroup

	install -d ${D}${datadir}/${BPN}
	install -m 0755 ${S}/lvrt-cgroup.sh ${D}${datadir}/${BPN}/lvrt-cgroup.sh
}

# Restore direct-call entry points after systemd.bbclass removes init.d.
MOUNTUTILS_INSTALL = ""
MOUNTUTILS_INSTALL:xilinx-zynq = "install_mountutils"
do_install[postfuncs] += "install_compat_links ${MOUNTUTILS_INSTALL}"

install_compat_links () {
	install -d ${D}${sysconfdir}/init.d
	for script in mountconfig populateconfig; do
		ln -rs ${D}${libdir}/systemd/scripts/$script ${D}${sysconfdir}/init.d/$script
	done
}

install_mountutils () {
	install -D -m 0755 ${S}/mountutils ${D}${sysconfdir}/init.d/mountutils
}

do_install:append:xilinx-zynq () {
	install -m 0644 ${S}/nisetbootcount.service ${D}${systemd_unitdir}/system
	install -m 0755 ${UNPACKDIR}/nisetbootcount ${D}${libdir}/systemd/scripts
	install -m 0644 ${S}/niusbgadget.service ${D}${systemd_unitdir}/system
	install -m 0755 ${UNPACKDIR}/niusbgadget ${D}${libdir}/systemd/scripts
}

FILES:${PN} += " \
	${systemd_unitdir}/system/cleanvarcache.service \
	${systemd_unitdir}/system/firewall.service \
	${libdir}/systemd/scripts/firewall \
	${sysconfdir}/default/lvrt-cgroup \
	${datadir}/${BPN}/lvrt-cgroup.sh \
	${systemd_unitdir}/system/mountconfig.service \
	${libdir}/systemd/scripts/mountconfig \
	${sysconfdir}/init.d/mountconfig \
	${systemd_unitdir}/system/nicheckbiosconfig.service \
	${libdir}/systemd/scripts/nicheckbiosconfig \
	${systemd_unitdir}/system/nicleanefivars.service \
	${systemd_unitdir}/system/nicleanstalelinks.service \
	${systemd_unitdir}/system/nicreatecpuacctgroups.service \
	${libdir}/systemd/scripts/nicreatecpuacctgroups \
	${systemd_unitdir}/system/nicreatecpusets.service \
	${libdir}/systemd/scripts/nicreatecpusets \
	${systemd_unitdir}/system/niconfiguretracefs.service \
	${libdir}/systemd/scripts/niconfiguretracefs \
	${systemd_unitdir}/system/nidisablecstates.service \
	${systemd_unitdir}/system/nisetcommitratio.service \
	${libdir}/systemd/scripts/nisetcommitratio \
	${systemd_unitdir}/system/nisetreboottype.service \
	${libdir}/systemd/scripts/nisetreboottype \
	${systemd_unitdir}/system/nisetupkernelconfig.service \
	${libdir}/systemd/scripts/nisetupkernelconfig \
	${systemd_unitdir}/system/nipopulateconfigdir.service \
	${systemd_unitdir}/system/populateconfig.service \
	${libdir}/systemd/scripts/populateconfig \
	${sysconfdir}/init.d/populateconfig \
	${systemd_unitdir}/system/wirelesssetdomain.service \
	${libdir}/systemd/scripts/wirelesssetdomain \
"

FILES:${PN}:append:xilinx-zynq = " \
	${sysconfdir}/init.d/mountutils \
	${systemd_unitdir}/system/nisetbootcount.service \
	${libdir}/systemd/scripts/nisetbootcount \
	${systemd_unitdir}/system/niusbgadget.service \
	${libdir}/systemd/scripts/niusbgadget \
"

SYSTEMD_SERVICE:${PN}:append:xilinx-zynq = " nisetbootcount.service niusbgadget.service"

RDEPENDS:${PN} += "\
	bash \
	iw \
	niacctbase \
	ni-cgroups-selector \
"
