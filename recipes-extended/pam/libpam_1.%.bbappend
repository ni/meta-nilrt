FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI += "\
	file://security/faillock.conf \
"

do_install:append() {
	install -m 644 ${WORKDIR}/security/faillock.conf ${D}${sysconfdir}/security/faillock.conf
}

pkg_postinst:pam-plugin-faillock:append() {
	# enable faillock
	sed -E -i 's/^(.+)success=1(.+)$/auth    requisite pam_faillock.so preauth\n\1success=2\2\nauth    [default=die] pam_faillock.so authfail/' "${sysconfdir}/pam.d/common-auth"
	echo "auth    sufficient pam_faillock.so authsucc" >> "${sysconfdir}/pam.d/common-auth"
}

pkg_prerm:pam-plugin-faillock:append() {
	# disable faillock
	sed -E -i '/pam_faillock.so/d' "${sysconfdir}/pam.d/common-auth"
	sed -E -i 's/^(.+)success=2(.+)$/\1success=1\2/' "${sysconfdir}/pam.d/common-auth"
}

RCONFLICTS:pam-plugin-faillock:append = " ni-auth"
