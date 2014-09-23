FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

hostname = ""

SRC_URI += "file://natinst-path.sh"
SRC_URI += "file://safemode-ps1.sh"

do_install_append () {
	mkdir -p ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/natinst-path.sh ${D}${sysconfdir}/profile.d/
	install ${WORKDIR}/safemode-ps1.sh ${D}${sysconfdir}/profile.d/

	install -d ${D}${sysconfdir}/default/volatiles
	echo "d root root 0755 /var/volatile/cache none" \
		>> ${D}${sysconfdir}/default/volatiles/10_varcache
	echo "l root root 0755 /var/cache /var/volatile/cache" \
		>> ${D}${sysconfdir}/default/volatiles/10_varcache

	echo "d ${LVRT_USER} ${LVRT_GROUP} 0775 /run/natinst none" \
		>> ${D}${sysconfdir}/default/volatiles/20_run_natinst
}

pkg_postinst_${PN} () {
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
