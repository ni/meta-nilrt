
FILES_${PN}-sshd += "${sysconfdir}/ssh/ssh_host_dsa_key \
		     ${sysconfdir}/ssh/ssh_host_dsa_key.pub \
		     ${sysconfdir}/ssh/ssh_host_ecdsa_key \
		     ${sysconfdir}/ssh/ssh_host_ecdsa_key.pub \
		     ${sysconfdir}/ssh/ssh_host_rsa_key \
		     ${sysconfdir}/ssh/ssh_host_rsa_key.pub \
		     ${sysconfdir}/ssh/ssh_host_ed25519_key \
		     ${sysconfdir}/ssh/ssh_host_ed25519_key.pub \
"

do_install_append () {
    # ssh keys symlinks
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_dsa_key ${D}${sysconfdir}/ssh/ssh_host_dsa_key
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_dsa_key.pub ${D}${sysconfdir}/ssh/ssh_host_dsa_key.pub
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_ecdsa_key ${D}${sysconfdir}/ssh/ssh_host_ecdsa_key
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_ecdsa_key.pub ${D}${sysconfdir}/ssh/ssh_host_ecdsa_key.pub
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_rsa_key ${D}${sysconfdir}/ssh/ssh_host_rsa_key
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_rsa_key.pub ${D}${sysconfdir}/ssh/ssh_host_rsa_key.pub
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_ed25519_key ${D}${sysconfdir}/ssh/ssh_host_ed25519_key
    ln -s ${sysconfdir}/natinst/share/ssh/ssh_host_ed25519_key.pub ${D}${sysconfdir}/ssh/ssh_host_ed25519_key.pub

    # customize sshd_config
    sed -e 's|^[#[:space:]]*Banner.*|Banner /etc/issue.net|' \
			-e 's|^[#[:space:]]*UseDNS.*|UseDNS no|' \
			-e 's|^[#[:space:]]*PasswordAuthentication.*|PasswordAuthentication no|' \
			-i ${D}${sysconfdir}/ssh/sshd_config
}
