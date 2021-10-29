FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

DEPENDS_append_class-target += "shadow-native pseudo-native"

RDEPENDS_${PN}-hwclock_append += "niacctbase busybox-hwclock"
RDEPENDS_${PN}-ptest += "${PN}-nilrt-ptest"

pkg_postinst_${PN}-hwclock () {
	chmod 4550 $D${base_sbindir}/hwclock.${BPN}
	chown 0:${LVRT_GROUP} $D${base_sbindir}/hwclock.${BPN}
	update-alternatives --install ${base_sbindir}/hwclock hwclock ${base_sbindir}/hwclock.${BPN} 80
}

pkg_postinst_ontarget_${PN}-hwclock () {
        if [ ! -f /etc/natinst/safemode ]; then
            setcap CAP_SYS_TIME+ep ${base_sbindir}/hwclock.util-linux
        fi
}
