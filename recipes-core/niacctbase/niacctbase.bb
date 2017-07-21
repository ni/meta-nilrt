DESCRIPTION = "Basic user and group settings for NI Linux RT applications."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

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
USERADD_PARAM_${PN} = " -m -N -g ${LVRT_GROUP} -G niwscerts,plugdev,tty -c 'LabVIEW user' ${LVRT_USER}; \
		-N -g openvpn -G network -c 'OpenVPN' -r openvpn; \
		${@base_conditional('DISTRO', 'nilrt-nxg', '', d.getVar('USERADD_WEBSERVICES', True), d)}"

useradd_preinst_append () {
    eval ${PSEUDO} chmod g+s ${SYSROOT}/home/${LVRT_USER} || true
    # this logic is only for nilrt and nilrt-xfce
    if ${@base_conditional('DISTRO', 'nilrt-nxg', 'false', 'true', d)}; then
	eval ${PSEUDO} chmod g+s ${SYSROOT}/home/webserv || true
	eval ${PSEUDO} ln -sf /home/admin ${SYSROOT}/home/root || true
    fi
}

BBCLASSEXTEND = "native nativesdk"
