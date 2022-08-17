FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

DEPENDS:append:class-target += "shadow-native pseudo-native"

RDEPENDS:${PN}-hwclock_append += "niacctbase busybox-hwclock"
RDEPENDS:${PN}-ptest += "${PN}-nilrt-ptest"

pkg_postinst:${PN}-hwclock () {
	chmod 4550 $D${base_sbindir}/hwclock.${BPN}
	chown 0:${LVRT_GROUP} $D${base_sbindir}/hwclock.${BPN}
	update-alternatives --install ${base_sbindir}/hwclock hwclock ${base_sbindir}/hwclock.${BPN} 80
}

pkg_postinst:ontarget:${PN}-hwclock () {
		if [ ! -f /etc/natinst/safemode ]; then
			setcap CAP_SYS_TIME+ep ${base_sbindir}/hwclock.util-linux
		fi
}
