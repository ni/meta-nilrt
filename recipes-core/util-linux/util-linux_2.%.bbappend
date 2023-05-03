FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-skip-btrfs-tests-if-kernel-support-for-btrfs-is-miss.patch"

DEPENDS:append:class-target = " shadow-native pseudo-native busybox"

RDEPENDS:${PN}-hwclock:append = " niacctbase busybox-hwclock"
RDEPENDS:${PN}-ptest += "${PN}-nilrt-ptest"

pkg_postinst:${PN}-hwclock () {
	chmod 4550 $D${base_sbindir}/hwclock.${BPN}
	chown 0:${LVRT_GROUP} $D${base_sbindir}/hwclock.${BPN}
	update-alternatives --install ${base_sbindir}/hwclock hwclock ${base_sbindir}/hwclock.${BPN} 80
}

pkg_postinst_ontarget:${PN}-hwclock () {
        if [ ! -f /etc/natinst/safemode ]; then
            setcap CAP_SYS_TIME+ep ${base_sbindir}/hwclock.util-linux
        fi
}
