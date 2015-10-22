DESCRIPTION = "Basic user and group settings for NI Linux RT applications."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r1"

ALLOW_EMPTY_${PN} = "1"

inherit allarch useradd

USERADD_PACKAGES = "${PN}"

# Note: With the parsing that happens with these varaibles, you need to be sure
# to avoid "... ;", no trailing semicolons
GROUPADD_PARAM_${PN} = " --system -g 500 ${LVRT_GROUP}; \
	--system -g 499 ${OPENVPN_GROUP}; \
	--system -g 498 niwscerts"

# add parameter -m if you want home directories created with default files (.profile, .bashrc)
USERADD_PARAM_${PN} = " -m -u 500 -N -g ${LVRT_GROUP} -G niwscerts,plugdev,tty -c 'LabVIEW user' ${LVRT_USER}; \
		-u 499 -N -g ${OPENVPN_GROUP} -c 'OpenVPN user' -r ${OPENVPN_USER}"

useradd_preinst_append () {
eval ${PSEUDO} chmod g+s ${SYSROOT}/home/${LVRT_USER} || true
}
