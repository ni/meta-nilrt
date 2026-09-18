FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

DEPENDS:append:class-target = " shadow-native pseudo-native busybox"

RDEPENDS:${PN}-hwclock:append = " niacctbase busybox-hwclock"
RDEPENDS:${PN}-ptest += "${PN}-nilrt-ptest"

# The losetup-loop "explicit-rw" subtest fails on NILRT because findmnt also
# reports the ext default mount options (errors=continue,user_xattr,acl). Drop
# just that subtest; the other 8 still run. See AB#3429527.
do_install_ptest:append() {
	sed -i '/^ts_init_subtest "explicit-rw"/,/^ts_finalize_subtest$/d' \
		${D}${PTEST_PATH}/tests/ts/libmount/loop
	rm -f ${D}${PTEST_PATH}/tests/expected/libmount/loop-explicit-rw
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
