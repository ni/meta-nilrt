FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

hostname = ""

SRC_URI += "file://natinst-path.sh \
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
		ln -sf lib ${D}/lib64
		ln -sf lib ${D}/usr/local/natinst/lib64
	fi

	install -d ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/natinst-path.sh ${D}${sysconfdir}/profile.d/

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
}
