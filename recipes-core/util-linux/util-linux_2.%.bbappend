FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

DEPENDS:append:class-target = " shadow-native pseudo-native busybox"

RDEPENDS:${PN}-hwclock:append = " niacctbase busybox-hwclock"
RDEPENDS:${PN}-ptest += "${PN}-nilrt-ptest"

# libmount "losetup-loop" (tests/ts/libmount/loop) is unreliable on NILRT targets
# and fails the functional ptest run. Disable it. See AzDO work item 3429527.
do_install_ptest:append() {
	rm -rf ${D}${PTEST_PATH}/tests/ts/libmount/loop
}

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
