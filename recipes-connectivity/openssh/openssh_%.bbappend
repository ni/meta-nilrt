do_install_append () {
    # customize sshd_config
    sed -e 's|^[#[:space:]]*Banner.*|Banner /etc/issue.net|' \
			-e 's|^[#[:space:]]*UseDNS.*|UseDNS no|' \
			-e 's|^[#[:space:]]*PasswordAuthentication.*|PasswordAuthentication no|' \
			-e 's|^[#[:space:]]*PermitRootLogin.*|PermitRootLogin yes|' \
			-i ${D}${sysconfdir}/ssh/sshd_config
}
