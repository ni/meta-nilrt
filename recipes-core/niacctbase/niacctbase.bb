DESCRIPTION = "Basic user and group settings for NI Linux RT applications."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

ALLOW_EMPTY_${PN} = "1"

inherit allarch useradd

USERADD_PACKAGES = "${PN}"

# Note: With the parsing that happens with these varaibles, you need to be sure
# to avoid "... ;", no trailing semicolons
GROUPADD_PARAM_${PN} = " --system ${LVRT_GROUP}; \
	--system openvpn; \
	--system niwscerts; \
	--system network"

# this is only used in nilrt and nilrt-xfce distros
USERADD_WEBSERVICES = "-m -N -g ${LVRT_GROUP} -G niwscerts,plugdev,adm,tty -c 'Web services user' webserv"

# add parameter -m if you want home directories created with default files (.profile, .bashrc)
USERADD_PARAM_${PN} = " -m -N -g ${LVRT_GROUP} -G network,niwscerts,plugdev,tty,video -c 'LabVIEW user' ${LVRT_USER}; \
		-N -g openvpn -G network -c 'OpenVPN' -r openvpn; \
		${@oe.utils.conditional('DISTRO', 'nilrt-nxg', '', d.getVar('USERADD_WEBSERVICES', True), d)}"

useradd_preinst_append () {
    eval ${PSEUDO} chmod g+s ${SYSROOT}/home/${LVRT_USER} || true
    # this logic is only for nilrt and nilrt-xfce
    if ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
	eval ${PSEUDO} chmod g+s ${SYSROOT}/home/webserv || true
	eval ${PSEUDO} ln -sf /home/admin ${SYSROOT}/home/root || true
    fi
}

BBCLASSEXTEND = "native nativesdk"
