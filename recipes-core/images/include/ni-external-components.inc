IMAGE_INSTALL_NODEPS_append = " \
	${@bb.utils.contains('DISTRO', 'nilrt-nxg', 'ni-sysmgmt-salt-minion-support', '', d)} \
"
