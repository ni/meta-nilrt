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

	echo "d ${LVRT_USER} ${LVRT_GROUP} 0775 /var/volatile/run/natinst none" \
		>> ${D}${sysconfdir}/default/volatiles/20_run_natinst
}
