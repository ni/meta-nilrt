SUMMARY = "Common user and group settings for NI applications"
DESCRIPTION = "Configures the '${LVRT_USER}' user account; the '${LVRT_GROUP}' group; and other common, NI-specific accounts. Configures privileges for members of those accounts."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.1"

SRC_URI = "\
	file://sudoers \
	file://udev.rules \
"

S = "${WORKDIR}"

ALLOW_EMPTY:${PN} = "1"


# useradd #
inherit allarch useradd

USERADD_PACKAGES = "${PN}"

# Note: With the parsing that happens with these varaibles, you need to be sure
# to avoid "... ;", no trailing semicolons
GROUPADD_PARAM:${PN} = " \
	--system ${LVRT_GROUP}; \
	--system openvpn; \
	--system niwscerts; \
	--system network"

# add parameter -m if you want home directories created with default files (.profile, .bashrc)
USERADD_PARAM:${PN} = " \
	-m -N -g ${LVRT_GROUP} -G network,niwscerts,plugdev,tty,video -c 'LabVIEW user' ${LVRT_USER}; \
	-m -N -g ${LVRT_GROUP} -G niwscerts,plugdev,adm,tty -c 'Web services user' webserv; \
	-N -g openvpn -G network -c 'OpenVPN' -r openvpn; \
"

useradd_preinst:append () {
	eval ${PSEUDO} chmod g+sw ${SYSROOT}/home/${LVRT_USER} || true
	eval ${PSEUDO} chmod g+s ${SYSROOT}/home/webserv || true
	eval ${PSEUDO} ln -sf /home/admin ${SYSROOT}/home/root || true
}


do_install:append () {
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
SUMMARY:${PN}-sudo = "${SUMMARY} - sudo integration"
FILES:${PN}-sudo = "${sysconfdir}/sudoers.d/*"
CONFFILES:${PN}-sudo = "${sysconfdir}/sudoers.d/*"
RDEPENDS:${PN}-sudo = "\
	niacctbase \
	sudo-lib \
"

# -udev : udev rules for the `ni` group
SUMMARY:${PN}-udev = "${SUMMARY} - udev rules"
FILES:${PN}-udev = "${sysconfdir}/udev/*"
CONFFILES:${PN}-udev = "${sysconfdir}/udev/rules.d/*"
RDEPENDS:${PN}-udev += " niacctbase udev"


RDEPENDS:${PN} += " ${PN}-udev"
RRECOMMENDS:${PN} += " ${PN}-sudo"
BBCLASSEXTEND = "native nativesdk"
