FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

# override the hostname var to avoid upstream base-files recipe creating
# /etc/hostname with its default value (set to ${MACHINE}) because we want
# the /etc/init.d/hostname.sh initscript to create it at runtime based on
# the firmware provided values, different for each device
hostname = ""

SRC_URI += "\
	file://natinst-path.sh \
	file://safemode-ps1.sh \
"

do_install_append () {
	install -d ${D}/usr/local/natinst/lib/

	# Create empty directory for user libraries
	install -d ${D}/usr/local/lib/

	# Symlink /lib64 to /lib on x86_64
	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		ln -sf lib ${D}/lib64
		install -d ${D}/usr/local/natinst
		ln -sf lib ${D}/usr/local/natinst/lib64
	fi

	install -d ${D}${sysconfdir}/profile.d/
	install -m 0644 ${WORKDIR}/natinst-path.sh ${D}${sysconfdir}/profile.d/

	if ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'true', 'false', d)}; then
		# only for newer NILRT
		install -m 644 ${WORKDIR}/issue.net  ${D}${sysconfdir}
	fi

	install ${WORKDIR}/safemode-ps1.sh ${D}${sysconfdir}/profile.d/

	install -d ${D}${sysconfdir}/default/volatiles/
	echo "d root root 0755 /var/volatile/cache none" \
		>> ${D}${sysconfdir}/default/volatiles/10_varcache
	echo "l root root 0755 /var/cache /var/volatile/cache" \
		>> ${D}${sysconfdir}/default/volatiles/10_varcache

	echo "d ${LVRT_USER} ${LVRT_GROUP} 0775 /run/natinst none" \
		>> ${D}${sysconfdir}/default/volatiles/20_run_natinst
}
