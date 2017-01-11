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
	--system ${OPENVPN_GROUP}; \
	--system niwscerts; \
	--system network"

# add parameter -m if you want home directories created with default files (.profile, .bashrc)
USERADD_PARAM_${PN} = " -m -N -g ${LVRT_GROUP} -G niwscerts,plugdev,tty -c 'LabVIEW user' ${LVRT_USER}; \
		-N -g ${OPENVPN_GROUP} -G network -c 'OpenVPN user' -r ${OPENVPN_USER}"

useradd_preinst_append () {
eval ${PSEUDO} chmod g+s ${SYSROOT}/home/${LVRT_USER} || true
}
