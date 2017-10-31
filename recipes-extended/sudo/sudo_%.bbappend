do_install_append() {
	# only for nilrt and nilrt-xfce
	if ${@base_conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
		echo >>"${D}${sysconfdir}/sudoers" ""
		echo >>"${D}${sysconfdir}/sudoers" "admin ALL=(ALL) NOPASSWD: ALL"
	fi
}
