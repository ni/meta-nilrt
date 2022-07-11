DESCRIPTION = "Basic user and group settings for NI Linux RT applications."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.1"

SRC_URI = "\
	file://sudoers \
	file://udev.rules \
"

S = "${WORKDIR}"

ALLOW_EMPTY_${PN} = "1"


# useradd #
inherit allarch useradd

USERADD_PACKAGES = "${PN}"

# Note: With the parsing that happens with these varaibles, you need to be sure
# to avoid "... ;", no trailing semicolons
GROUPADD_PARAM_${PN} = " \
	--system ${LVRT_GROUP}; \
	--system openvpn; \
	--system niwscerts; \
	--system network"

# add parameter -m if you want home directories created with default files (.profile, .bashrc)
USERADD_PARAM_${PN} = " \
	-m -N -g ${LVRT_GROUP} -G network,niwscerts,plugdev,tty,video -c 'LabVIEW user' ${LVRT_USER}; \
	-m -N -g ${LVRT_GROUP} -G niwscerts,plugdev,adm,tty -c 'Web services user' webserv; \
	-N -g openvpn -G network -c 'OpenVPN' -r openvpn; \
"

useradd_preinst_append () {
	eval ${PSEUDO} chmod g+sw ${SYSROOT}/home/${LVRT_USER} || true
	eval ${PSEUDO} chmod g+s ${SYSROOT}/home/webserv || true
	eval ${PSEUDO} ln -sf /home/admin ${SYSROOT}/home/root || true
}


do_install_append () {
	install -d ${D}${sysconfdir}/sudoers.d/
	install --mode=0660 ${S}/sudoers ${D}${sysconfdir}/sudoers.d/${PN}

	install -d ${D}${sysconfdir}/udev/rules.d
	install --mode=0644 ${S}/udev.rules ${D}${sysconfdir}/udev/rules.d/90-${PN}.rules
}


## subpackages
PACKAGE_BEFORE_PN += " \
	${PN}-sudo \
	${PN}-udev \
"
# -sudo : sudo integration
SUMMARY_${PN}-sudo = "${SUMMARY} - sudo integration"
FILES_${PN}-sudo = "${sysconfdir}/sudoers.d/*"
CONFFILES_${PN}-sudo = "${sysconfdir}/sudoers.d/*"
RDEPENDS_${PN}-sudo = "\
	niacctbase \
	sudo-lib \
"

# -udev : udev rules for the `ni` group
SUMMARY_${PN}-udev = "${SUMMARY} - udev rules"
FILES_${PN}-udev = "${sysconfdir}/udev/*"
CONFFILES_${PN}-udev = "${sysconfdir}/udev/rules.d/*"
RDEPENDS_${PN}-udev += " udev"


RDEPENDS_${PN} += " ${PN}-udev"
RRECOMMENDS_${PN} += " ${PN}-sudo"
BBCLASSEXTEND = "native nativesdk"
