do_install_append () {
    # customize sshd_config
    sed -e 's|^[#[:space:]]*Banner.*|Banner /etc/issue.net|' \
			-e 's|^[#[:space:]]*UseDNS.*|UseDNS no|' \
			-e 's|^[#[:space:]]*PasswordAuthentication.*|PasswordAuthentication no|' \
			-i ${D}${sysconfdir}/ssh/sshd_config
}
