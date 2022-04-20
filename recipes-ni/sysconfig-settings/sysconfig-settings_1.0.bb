SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

uixmldir = "${datadir}/nisysapi/uixml"
settingsdatadir = "${datadir}/${BPN}/systemsettings"
systemsettingsdir = "${localstatedir}/local/natinst/systemsettings"

inherit update-rc.d

# sysconfig-settings package
SRC_URI = "file://systemsettings/consoleout.ini \
           file://systemsettings/fpga_target.ini \
           file://systemsettings/rt_target.ini \
           file://systemsettings/target_common.ini \
           file://uixml/nilinuxrt.rtprotocol_enable.binding.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.de.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.fr.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.ja.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.ko.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.zh-CN.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.def.xml \
           file://uixml/nilinuxrt.rtapp_disable.binding.xml \
           file://uixml/nilinuxrt.rtapp_disable.const.xml \
           file://uixml/nilinuxrt.rtapp_disable.const.de.xml \
           file://uixml/nilinuxrt.rtapp_disable.const.fr.xml \
           file://uixml/nilinuxrt.rtapp_disable.const.ja.xml \
           file://uixml/nilinuxrt.rtapp_disable.const.ko.xml \
           file://uixml/nilinuxrt.rtapp_disable.const.zh-CN.xml \
           file://uixml/nilinuxrt.rtapp_disable.def.xml \
           file://uixml/nilinuxrt.fpga_disable.binding.xml \
           file://uixml/nilinuxrt.fpga_disable.const.xml \
           file://uixml/nilinuxrt.fpga_disable.const.de.xml \
           file://uixml/nilinuxrt.fpga_disable.const.fr.xml \
           file://uixml/nilinuxrt.fpga_disable.const.ja.xml \
           file://uixml/nilinuxrt.fpga_disable.const.ko.xml \
           file://uixml/nilinuxrt.fpga_disable.const.zh-CN.xml \
           file://uixml/nilinuxrt.fpga_disable.def.xml \
"

FILES_${PN} = "${settingsdatadir}/consoleout.ini \
               ${settingsdatadir}/fpga_target.ini \
               ${settingsdatadir}/rt_target.ini \
               ${settingsdatadir}/target_common.ini \
               ${uixmldir}/nilinuxrt.rtprotocol_enable.* \
               ${uixmldir}/nilinuxrt.rtapp_disable.* \
               ${uixmldir}/nilinuxrt.fpga_disable.* \
               ${systemsettingsdir} \
"

DEPENDS += "shadow-native pseudo-native niacctbase base-files-nilrt"
RDEPENDS_${PN} += "niacctbase bash fw-printenv"

# sysconfig-settings-ssh package
PACKAGES += "${PN}-ssh"

SUMMARY_${PN}-ssh = "System configuration files for ssh"
DESCRIPTION_${PN}-ssh = "SSH configuration files for the National Instruments System Configuration subsystem."

SRC_URI_append = "file://uixml/nilinuxrt.sshd_enable.binding.xml \
                  file://uixml/nilinuxrt.sshd_enable.const.de.xml \
                  file://uixml/nilinuxrt.sshd_enable.const.fr.xml \
                  file://uixml/nilinuxrt.sshd_enable.const.ja.xml \
                  file://uixml/nilinuxrt.sshd_enable.const.ko.xml \
                  file://uixml/nilinuxrt.sshd_enable.const.xml \
                  file://uixml/nilinuxrt.sshd_enable.const.zh-CN.xml \
                  file://uixml/nilinuxrt.sshd_enable.def.xml \
"

FILES_${PN}-ssh = "${uixmldir}/nilinuxrt.sshd_enable.*"

# sysconfig-settings-ui package
PACKAGES += "${PN}-ui"

SUMMARY_${PN}-ui = "System configuration files to enable UI"
DESCRIPTION_${PN}-ui = "Configuration files to enable UI for the National Instruments System Configuration subsystem."

SRC_URI_append = "file://nisetembeddeduixml \
                  file://systemsettings/ui_enable.ini \
                  file://uixml/nilinuxrt.System.binding.xml \
                  file://uixml/nilinuxrt.System.const.xml \
                  file://uixml/nilinuxrt.System.const.de.xml \
                  file://uixml/nilinuxrt.System.const.fr.xml \
                  file://uixml/nilinuxrt.System.const.ja.xml \
                  file://uixml/nilinuxrt.System.const.ko.xml \
                  file://uixml/nilinuxrt.System.const.zh-CN.xml \
                  file://uixml/nilinuxrt.System.def.xml \
                  file://uixml/nilinuxrt.ui_enable.binding.xml \
                  file://uixml/nilinuxrt.ui_enable.const.xml \
                  file://uixml/nilinuxrt.ui_enable.const.de.xml \
                  file://uixml/nilinuxrt.ui_enable.const.fr.xml \
                  file://uixml/nilinuxrt.ui_enable.const.ja.xml \
                  file://uixml/nilinuxrt.ui_enable.const.ko.xml \
                  file://uixml/nilinuxrt.ui_enable.const.zh-CN.xml \
                  file://uixml/nilinuxrt.ui_enable.def.xml \
"

FILES_${PN}-ui = "${sysconfdir}/init.d/nisetembeddeduixml \
                  ${settingsdatadir}/ui_enable.ini \
                  ${uixmldir}/nilinuxrt.System.* \
                  ${uixmldir}/nilinuxrt.ui_enable.* \
"

RDEPENDS_${PN}-ui += "sysconfig-settings niacctbase"

INITSCRIPT_PACKAGES += "${PN}-ui"
INITSCRIPT_NAME_${PN}-ui = "nisetembeddeduixml"
INITSCRIPT_PARAMS_${PN}-ui = "start 20 5 ."

pkg_prerm_ontarget_${PN}-ui () {
	rm -f ${systemsettingsdir}/ui_enable.ini
}

do_install[depends] += "niacctbase:do_populate_sysroot"

S = "${WORKDIR}"

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

pkg_postinst_ontarget_${PN} () {
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

pkg_prerm_ontarget_${PN} () {
	rm -f ${systemsettingsdir}/target_common.ini \
	      ${systemsettingsdir}/rt_target.ini \
	      ${systemsettingsdir}/fpga_target.ini \
	      ${systemsettingsdir}/consoleout.ini
}
