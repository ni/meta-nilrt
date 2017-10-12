do_install_append() {
	# only for nilrt and nilrt-xfce
	if ${@base_conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
		echo "admin ALL=(ALL) ALL" >> ${D}${sysconfdir}/sudoers
	fi
}
