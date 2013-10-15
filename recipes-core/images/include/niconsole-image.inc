#add user(s) and group(s)
add_group() {
	# USAGE: add_group groupname groupid memberlist
	#  memberlist is a comma delimited list of usernames
	echo "$1:x:$2:$3" >> ${IMAGE_ROOTFS}/etc/group
	echo "$1:x::$3" >> ${IMAGE_ROOTFS}/etc/gshadow
}

add_system_user() {
	# USAGE: add_system_user username password userid groupid desc
	echo "$1:x:$3:$4:$5,,,:/home/$1:/bin/sh" >> ${IMAGE_ROOTFS}/etc/passwd
	echo "$1:$2:::::::" >> ${IMAGE_ROOTFS}/etc/shadow
}

add_user() {
	# USAGE: add_user username password userid groupid desc
	add_system_user $1 $2 $3 $4 $5
	mkdir ${IMAGE_ROOTFS}/home/$1
	chown $3.$4 ${IMAGE_ROOTFS}/home/$1
	chmod u+rwx,g-w,g+rs,o-w,o+rx ${IMAGE_ROOTFS}/home/$1
}

ROOTFS_POSTPROCESS_COMMAND += "add_group ni 500 ''; "
ROOTFS_POSTPROCESS_COMMAND += "add_user lvuser NP 500 500 'LabVIEW user'; "
ROOTFS_POSTPROCESS_COMMAND += "add_user webserv NP 501 500 'Web services user'; "
ROOTFS_POSTPROCESS_COMMAND += "add_group openvpn 499 ''; "
ROOTFS_POSTPROCESS_COMMAND += "add_system_user openvpn '*' 499 499 'OpenVPN'; "
ROOTFS_POSTPROCESS_COMMAND += "add_group niwscerts 498 'lvuser,webserv'; "

# set root password
# password is encrypted, use "cryptpw -a des <clear_text_password>" to get encrypted value
ROOTFS_POSTPROCESS_COMMAND += "sed -i -e's/root::/root:NP:/' ${IMAGE_ROOTFS}/etc/shadow; "

# mark hold packages
ROOTFS_POSTPROCESS_COMMAND += "opkg-cl ${IPKG_ARGS} flag hold libc6; "

