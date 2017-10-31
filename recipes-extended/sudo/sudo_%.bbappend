do_install_append() {
	# only for nilrt and nilrt-xfce
	if ${@base_conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
		echo >>"${D}${sysconfdir}/sudoers" ""
		echo >>"${D}${sysconfdir}/sudoers" "admin ALL=(ALL) NOPASSWD: ALL"
		echo >>"${D}${sysconfdir}/sudoers" ""
		echo >>"${D}${sysconfdir}/sudoers" "User_Alias TPM_QUOTERS = lvuser, webserv"
		echo >>"${D}${sysconfdir}/sudoers" "TPM_QUOTERS ALL=(#0:#0) NOPASSWD: /usr/sbin/nilrtdiskcrypt_quote -d"
		echo >>"${D}${sysconfdir}/sudoers" "TPM_QUOTERS ALL=(#0:#0) NOPASSWD: /usr/sbin/nilrtdiskcrypt_quote -j"
		echo >>"${D}${sysconfdir}/sudoers" "TPM_QUOTERS ALL=(#0:#0) NOPASSWD: /usr/sbin/nilrtdiskcrypt_quote -j -q 0x[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]"
	fi
}
