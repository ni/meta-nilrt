SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

DEPENDS += "niacctbase base-files-nilrt"

PV = "2.0"



SRC_URI = "\
	file://nisetembeddeduixml \
	file://systemsettings \
	file://uixml \
"

S = "${WORKDIR}"


inherit update-rc.d
INITSCRIPT_PACKAGES += "${PN}-ui"


uixmldir = "${datadir}/nisysapi/uixml"
settingsdatadir = "${datadir}/${BPN}/systemsettings"
systemsettingsdir = "${localstatedir}/local/natinst/systemsettings"

do_install[depends] += "niacctbase:do_populate_sysroot"

do_install () {
	# UIXML config (soft dip switches, etc.)
	install -d -m 0755 ${D}${uixmldir}/
	install -m 0644 ${S}/uixml/* ${D}${uixmldir}/

	# Common interface for system settings (soft dip switches, etc.)
	install -d -m 0775 ${D}${settingsdatadir}/
	install -m 0644 ${S}/systemsettings/* ${D}${settingsdatadir}/

	# Create shared systemsettingsdir with appropriate permissions and ownership
	install -d -m 0775 -o ${LVRT_USER} -g ${LVRT_GROUP} ${D}${systemsettingsdir}

	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/nisetembeddeduixml ${D}${sysconfdir}/init.d
}

pkg_postinst_ontarget:${PN} () {
	TARGET_CLASS=$(fw_printenv -n TargetClass 2>&1)

	ln -sf ${settingsdatadir}/target_common.ini ${systemsettingsdir}/target_common.ini
	ln -sf ${settingsdatadir}/rt_target.ini ${systemsettingsdir}/rt_target.ini

	# cDAQ targets should not have FPGA startup settings, and CVS
	# targets do not support FPGA autoload.
	if ! [ "$TARGET_CLASS" = "cDAQ" -o "$TARGET_CLASS" = "CVS" ]; then
		ln -sf ${settingsdatadir}/fpga_target.ini ${systemsettingsdir}/fpga_target.ini
	fi

	# add console out if we have a firmware variable for it (x86_64 targets only)
	efiConsoleOutEnable=$(fw_printenv -n BootFirmwareConsoleOutEnable 2>/dev/null || true)
	if ! [ -z "$efiConsoleOutEnable" ]; then
		ln -sf ${settingsdatadir}/consoleout.ini ${systemsettingsdir}/consoleout.ini
	fi
}

pkg_prerm_ontarget:${PN} () {
	rm -f \
		${systemsettingsdir}/target_common.ini \
		${systemsettingsdir}/rt_target.ini \
		${systemsettingsdir}/fpga_target.ini \
		${systemsettingsdir}/consoleout.ini
}


FILES:${PN} = "\
	${settingsdatadir}/consoleout.ini \
	${settingsdatadir}/fpga_target.ini \
	${settingsdatadir}/rt_target.ini \
	${settingsdatadir}/target_common.ini \
	${systemsettingsdir} \
	${uixmldir}/nilinuxrt.fpga_disable.* \
	${uixmldir}/nilinuxrt.rtapp_disable.* \
	${uixmldir}/nilinuxrt.rtprotocol_enable.* \
"
RDEPENDS:${PN} += "niacctbase bash fw-printenv"


## SUBPACKAGES
# sysconfig-settings-ssh package
SUMMARY:${PN}-ssh = "${SUMMARY} - sshd"
DESCRIPTION:${PN}-ssh = "SSH configuration files for the National Instruments System Configuration subsystem."
PACKAGES += "${PN}-ssh"
FILES:${PN}-ssh = "${uixmldir}/nilinuxrt.sshd_enable.*"


# sysconfig-settings-ui package
SUMMARY:${PN}-ui = "${SUMMARY} - enable UI"
DESCRIPTION:${PN}-ui = "Configuration files to enable UI for the National Instruments System Configuration subsystem."
PACKAGES += "${PN}-ui"
FILES:${PN}-ui = "\
	${settingsdatadir}/ui_enable.ini \
	${sysconfdir}/init.d/nisetembeddeduixml \
	${uixmldir}/nilinuxrt.System.* \
	${uixmldir}/nilinuxrt.ui_enable.* \
"
RDEPENDS:${PN}-ui += "sysconfig-settings niacctbase"

INITSCRIPT_NAME:${PN}-ui = "nisetembeddeduixml"
INITSCRIPT_PARAMS:${PN}-ui = "start 20 5 ."

pkg_prerm_ontarget:${PN}-ui () {
	rm -f ${systemsettingsdir}/ui_enable.ini
}

