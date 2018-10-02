IMAGE_INSTALL_NODEPS_append = " \
	${@bb.utils.contains('DISTRO', 'nilrt-nxg', 'ni-sysmgmt-salt-minion-support', '', d)} \
"

IMAGE_INSTALL_NODEPS_append_x64 = " \
	${@bb.utils.contains('DISTRO', 'nilrt-nxg', 'nisdmon', '', d)} \
"
