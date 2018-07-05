FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

DEPENDS_class-target += "shadow-native pseudo-native niacctbase"

RDEPENDS_util-linux-hwclock += "niacctbase"

group = "${LVRT_GROUP}"

pkg_postinst_${PN}-hwclock () {
	chmod 4550 $D${base_sbindir}/hwclock.${BPN}
	chown 0:${group} $D${base_sbindir}/hwclock.${BPN}
	update-alternatives --install ${base_sbindir}/hwclock hwclock ${base_sbindir}/hwclock.${BPN} 80
}

# To delay the execution of the postinst to first boot, check $D and error
# if empty. Process explained in the Yocto Manual Post-Installation Scripts
# section.
pkg_postinst_util-linux-hwclock () {
	if [ x"$D" = "x" ]; then
        if [ ! -f /etc/natinst/safemode ]; then
            setcap CAP_SYS_TIME+ep ${base_sbindir}/hwclock.util-linux
        fi
	else
		exit 1
	fi
}
