FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://close-all-ssh-connections-patch \
"


do_patch_oe_source () {
	# Patch sysvinit file to close all open ssh connections on shutdown or reboot
	#
	# The typical do_patch logic will not work for this file since the do_patch logic is designed to
	# patch the source code for openssh, and the file being patched is part of the recipe that builds
	# the openssh IPK.
	patch -u ${WORKDIR}/init -i ${WORKDIR}/close-all-ssh-connections-patch
}
addtask patch_oe_source after do_patch before do_configure

do_install:append () {

	# customize sshd_config
	sed -e 's|^[#[:space:]]*Banner .*|Banner /etc/issue.net|' \
		-e 's|^[#[:space:]]*UseDNS .*|UseDNS no|' \
		-e 's|^[#[:space:]]*PasswordAuthentication .*|PasswordAuthentication yes|' \
		-e 's|^[#[:space:]]*PermitEmptyPasswords .*|PermitEmptyPasswords yes|' \
		-e 's|^[#[:space:]]*PermitRootLogin .*|PermitRootLogin yes|' \
		-e 's|^[#[:space:]]*ChallengeResponseAuthentication .*|ChallengeResponseAuthentication no|' \
		-e '/.*HostKey.*/d' \
			-i ${D}${sysconfdir}/ssh/sshd_config

		echo                                                       >>${D}${sysconfdir}/ssh/sshd_config
		echo "# HostKeys for protocol version 2"                   >>${D}${sysconfdir}/ssh/sshd_config
		echo "HostKey /etc/natinst/share/ssh/ssh_host_rsa_key"     >>${D}${sysconfdir}/ssh/sshd_config
		echo "HostKey /etc/natinst/share/ssh/ssh_host_ecdsa_key"   >>${D}${sysconfdir}/ssh/sshd_config
		echo "HostKey /etc/natinst/share/ssh/ssh_host_ed25519_key" >>${D}${sysconfdir}/ssh/sshd_config
}
