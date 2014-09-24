FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

hostname = ""

SRC_URI += "file://natinst-path.sh \
	    file://safemode-ps1.sh \
	    file://functions.common \
	    file://udhcpc.script \
	    file://zcip.script \
"

RDEPENDS_${PN} += "niacctbase"

user = "${LVRT_USER}"
group = "${LVRT_GROUP}"

do_install_append () {
	install -d ${D}/usr/local/natinst/lib/

	# Create empty directory for user libraries
	install -d ${D}/usr/local/lib/

	# create log directory
	install -d -m 0755 ${D}${localstatedir}/local/natinst/log/

	# Symlink /lib64 to /lib on x86_64
	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		ln -s /lib ${D}/lib64
		ln -s /usr/local/natinst/lib ${D}/usr/local/natinst/lib64
	fi

	install -d ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/natinst-path.sh ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/safemode-ps1.sh ${D}${sysconfdir}/profile.d/

	# scripts for network configuration
	install -d ${D}${sysconfdir}/natinst/networking/
	install -m 0755 ${WORKDIR}/functions.common ${D}${sysconfdir}/natinst/networking/
	install -m 0755 ${WORKDIR}/udhcpc.script ${D}${sysconfdir}/natinst/networking/
	install -m 0755 ${WORKDIR}/zcip.script ${D}${sysconfdir}/natinst/networking/

	install -d ${D}${sysconfdir}/default/volatiles/
	echo "d root root 0755 /var/volatile/cache none" \
		>> ${D}${sysconfdir}/default/volatiles/10_varcache
	echo "l root root 0755 /var/cache /var/volatile/cache" \
		>> ${D}${sysconfdir}/default/volatiles/10_varcache

	echo "d ${LVRT_USER} ${LVRT_GROUP} 0775 /run/natinst none" \
		>> ${D}${sysconfdir}/default/volatiles/20_run_natinst

        # Overwrite changes to issue.net on do_install_basefilesissue
        install -m 644 ${WORKDIR}/issue.net  ${D}${sysconfdir}
}

pkg_postinst_${PN} () {
	# Change ownership on postinst instead of at build time to avoid circular dependency 
	# between niacctbase and base-files
	chown ${user}:${group} $D/var/local/natinst
	chown ${user}:${group} $D/var/local/natinst/log

	sed -e 's/^passwd:\s*compat/passwd:         niauth [!SUCCESS=continue] compat/' \
		-e 's/^group:\s*compat/group:          niauth [!SUCCESS=continue] compat/' \
		-e 's/^shadow:\s*compat/shadow:         niauth [!SUCCESS=continue] compat/' \
		-i $D${sysconfdir}/nsswitch.conf
}

pkg_prerm_${PN} () {
	sed -e 's/passwd:         niauth [!SUCCESS=continue] compat/passwd:         compat/' \
		-e 's/^group:          niauth [!SUCCESS=continue] compat/group:         compat/' \
		-e 's/^shadow:         niauth [!SUCCESS=continue] compat/shadow:         compat/' \
		-i $D${sysconfdir}/nsswitch.conf
}
