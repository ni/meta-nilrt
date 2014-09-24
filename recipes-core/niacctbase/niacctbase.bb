DESCRIPTION = "Basic user and group settings for NI Linux RT applications."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r1"

ALLOW_EMPTY_${PN} = "1"

inherit allarch useradd

USERADD_PACKAGES = "${PN}"

GROUPADD_PARAM_${PN} = " --system -g 500 ${LVRT_GROUP}; \
	--system -g 499 ${OPENVPN_GROUP}; \
	--system -g 498 niwscerts"

USERADD_PARAM_${PN} = " -u 500 -N -g ${LVRT_GROUP} -G niwscerts,plugdev,tty -c 'LabVIEW user' ${LVRT_USER}; \
		-u 499 -N -g ${OPENVPN_GROUP} -c 'OpenVPN user' -r ${OPENVPN_USER}; \
		-u 501 -N -g ${LVRT_GROUP} -G niwscerts,plugdev,adm -c 'Web services user' webserv"

useradd_preinst_append () {
eval ${PSEUDO} chmod g+s ${SYSROOT}/home/${LVRT_USER} ${SYSROOT}/home/webserv || true
eval ${PSEUDO} ln -sf /home/admin ${SYSROOT}/home/root || true
}
