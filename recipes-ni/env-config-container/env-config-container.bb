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

S = "${WORKDIR}"

do_install () {
	install -d ${D}${base_sbindir}

	# Install wrapper scripts with .wrapper suffix (softlinks will be created by init)
	install -m 0550   ${S}/fw_printenv.wrapper ${D}${base_sbindir}/
	install -m 0550   ${S}/fw_setenv.wrapper   ${D}${base_sbindir}/
}

