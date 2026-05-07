SUMMARY = "Container-specific wrappers and env_config for fw_printenv"
DESCRIPTION = "Wrapper scripts and env_config file for containers"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"


RDEPENDS:${PN} = "bash"

SRC_URI = "\
	file://fw_printenv.wrapper \
	file://fw_setenv.wrapper \
"

S = "${UNPACKDIR}"

do_install () {
	install -d ${D}${base_sbindir}

	# Install wrapper scripts with .wrapper suffix (softlinks will be created by init)
	# fw_printenv needs group 'ni' (gid 500) execute permission because
	# SystemWebServer runs as webserv:ni and libnitargetcfg calls
	# /sbin/fw_printenv to read DeviceCode/DeviceDesc. Without execute
	# access, NI MAX shows model "Pele".
	install -m 0550 ${S}/fw_printenv.wrapper ${D}${base_sbindir}/
	chgrp 500 ${D}${base_sbindir}/fw_printenv.wrapper
	install -m 0550 ${S}/fw_setenv.wrapper ${D}${base_sbindir}/
}

