SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"


# RECIPE VARIABLES #
DEPENDS += "shadow-native pseudo-native niacctbase base-files-nilrt"


# SOURCE VARIABLES #
SRC_URI = "\
	file://systemsettings/fpga_target.ini \
	file://systemsettings/rt_target.ini \
	file://systemsettings/target_common.ini \
	file://uixml/nilinuxrt.fpga_disable.binding.xml \
	file://uixml/nilinuxrt.fpga_disable.const.de.xml \
	file://uixml/nilinuxrt.fpga_disable.const.fr.xml \
	file://uixml/nilinuxrt.fpga_disable.const.ja.xml \
	file://uixml/nilinuxrt.fpga_disable.const.ko.xml \
	file://uixml/nilinuxrt.fpga_disable.const.xml \
	file://uixml/nilinuxrt.fpga_disable.const.zh-CN.xml \
	file://uixml/nilinuxrt.fpga_disable.def.xml \
	file://uixml/nilinuxrt.rtapp_disable.binding.xml \
	file://uixml/nilinuxrt.rtapp_disable.const.de.xml \
	file://uixml/nilinuxrt.rtapp_disable.const.fr.xml \
	file://uixml/nilinuxrt.rtapp_disable.const.ja.xml \
	file://uixml/nilinuxrt.rtapp_disable.const.ko.xml \
	file://uixml/nilinuxrt.rtapp_disable.const.xml \
	file://uixml/nilinuxrt.rtapp_disable.const.zh-CN.xml \
	file://uixml/nilinuxrt.rtapp_disable.def.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.binding.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.const.de.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.const.fr.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.const.ja.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.const.ko.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.const.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.const.zh-CN.xml \
	file://uixml/nilinuxrt.rtprotocol_enable.def.xml \
"

S = "${UNPACKDIR}"

uixmldir = "${datadir}/nisysapi/uixml"
settingsdatadir = "${datadir}/${BPN}/systemsettings"
systemsettingsdir = "${localstatedir}/local/natinst/systemsettings"


# CLASS VARIABLES #
inherit update-rc.d


# TASK OVERRIDES #
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

	install -d ${D}${script_location}
	install -m 0755 ${S}/nisetembeddeduixml ${D}${script_location}
	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${S}/nisetembeddeduixml.service ${D}${systemd_system_unitdir}/
	fi
}

pkg_postinst_ontarget:${PN} () {
	TARGET_CLASS=$(fw_printenv -n TargetClass 2>&1)

	ln -sf ${settingsdatadir}/target_common.ini ${systemsettingsdir}/target_common.ini
	# Ethernet RIO targets should not have RT startup settings
	if ! [ "$TARGET_CLASS" = "Ethernet RIO" ]; then
		ln -sf ${settingsdatadir}/rt_target.ini ${systemsettingsdir}/rt_target.ini
	fi

	# cDAQ targets should not have FPGA startup settings, and CVS
	# targets do not support FPGA autoload.
	if ! [ "$TARGET_CLASS" = "cDAQ" -o "$TARGET_CLASS" = "CVS" ]; then
		ln -sf ${settingsdatadir}/fpga_target.ini ${systemsettingsdir}/fpga_target.ini
	fi
}

pkg_prerm_ontarget:${PN} () {
	rm -f ${systemsettingsdir}/target_common.ini \
		${systemsettingsdir}/rt_target.ini \
		${systemsettingsdir}/fpga_target.ini
}


# PACKAGE VARAIBLES #
FILES:${PN} = "\
	${settingsdatadir}/fpga_target.ini \
	${settingsdatadir}/rt_target.ini \
	${settingsdatadir}/target_common.ini \
	${systemsettingsdir} \
	${uixmldir}/nilinuxrt.fpga_disable.* \
	${uixmldir}/nilinuxrt.rtapp_disable.* \
	${uixmldir}/nilinuxrt.rtprotocol_enable.* \
"

RDEPENDS:${PN} += "niacctbase bash fw-printenv"


# SUBPACKAGES #
###############

# sysconfig-settings-console package
PACKAGES += "${PN}-console"
SUMMARY:${PN}-console = "System configuration files to control console output"
DESCRIPTION:${PN}-console = "Configuration files that allow NI hardware configuration applications to control the consoleout firmware setting."

SRC_URI:append = "\
	file://systemsettings/consoleout.ini \
"

FILES:${PN}-console = "\
	${settingsdatadir}/consoleout.ini \
"

RDEPENDS:${PN}-console += "sysconfig-settings fw-printenv"


pkg_postinst_ontarget:${PN}-console:x64 () {
	# Add console out if we have a firmware variable for it
	efiConsoleOutEnable=$(fw_printenv -n BootFirmwareConsoleOutEnable 2>/dev/null || true)
	if ! [ -z "$efiConsoleOutEnable" ]; then
		ln -sf ${settingsdatadir}/consoleout.ini ${systemsettingsdir}/consoleout.ini
	fi
}

pkg_postinst_ontarget:${PN}-console:armv7a () {
	# Add console out if it's not an Ethernet RIO target
	TARGET_CLASS=$(fw_printenv -n TargetClass 2>&1)
	if ! [ "$TARGET_CLASS" = "Ethernet RIO" ]; then
		ln -sf ${settingsdatadir}/consoleout.ini ${systemsettingsdir}/consoleout.ini
	fi
}

pkg_prerm_ontarget:${PN}-console () {
	rm -f ${systemsettingsdir}/consoleout.ini
}

# sysconfig-settings-ui package
PACKAGES += "${PN}-ui"
SUMMARY:${PN}-ui = "System configuration files to enable UI"
DESCRIPTION:${PN}-ui = "Configuration files to enable UI for the National Instruments System Configuration subsystem."

SRC_URI:append = "\
	file://nisetembeddeduixml \
	file://nisetembeddeduixml.service \
	file://systemsettings/ui_enable.ini \
	file://uixml/nilinuxrt.System.binding.xml \
	file://uixml/nilinuxrt.System.const.de.xml \
	file://uixml/nilinuxrt.System.const.fr.xml \
	file://uixml/nilinuxrt.System.const.ja.xml \
	file://uixml/nilinuxrt.System.const.ko.xml \
	file://uixml/nilinuxrt.System.const.xml \
	file://uixml/nilinuxrt.System.const.zh-CN.xml \
	file://uixml/nilinuxrt.System.def.xml \
	file://uixml/nilinuxrt.ui_enable.binding.xml \
	file://uixml/nilinuxrt.ui_enable.const.de.xml \
	file://uixml/nilinuxrt.ui_enable.const.fr.xml \
	file://uixml/nilinuxrt.ui_enable.const.ja.xml \
	file://uixml/nilinuxrt.ui_enable.const.ko.xml \
	file://uixml/nilinuxrt.ui_enable.const.xml \
	file://uixml/nilinuxrt.ui_enable.const.zh-CN.xml \
	file://uixml/nilinuxrt.ui_enable.def.xml \
"

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN}-ui = "\
	${script_location}/nisetembeddeduixml \
	${settingsdatadir}/ui_enable.ini \
	${uixmldir}/nilinuxrt.System.* \
	${uixmldir}/nilinuxrt.ui_enable.* \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/nisetembeddeduixml.service','',d)} \
"

RDEPENDS:${PN}-ui += "sysconfig-settings niacctbase"

INITSCRIPT_PACKAGES += "${@bb.utils.contains('INIT_MANAGER','sysvinit','${PN}-ui','',d)}"
INITSCRIPT_NAME:${PN}-ui = "nisetembeddeduixml"
INITSCRIPT_PARAMS:${PN}-ui = "start 20 5 ."

inherit systemd
SYSTEMD_SERVICE:${PN}-ui = "nisetembeddeduixml.service"
SYSTEMD_AUTO_ENABLE:${PN}-ui = "enable"

pkg_prerm_ontarget:${PN}-ui () {
	nirtcfg --set "section=systemsettings,token=ui.enabled,value=False"
	rm -f ${systemsettingsdir}/ui_enable.ini
}

# sysconfig-settings-firewall package
PACKAGES += "${PN}-firewall"
SUMMARY:${PN}-firewall = "System configuration files to enable the firewall"
DESCRIPTION:${PN}-firewall = "Configuration files that allow NI hardware configuration applications to enable or disable firewalld (SystemSettings firewall.enabled)."

SRC_URI:append = "\
	file://systemsettings/firewall_enable.ini \
	file://uixml/nilinuxrt.firewall_enable.binding.xml \
	file://uixml/nilinuxrt.firewall_enable.const.de.xml \
	file://uixml/nilinuxrt.firewall_enable.const.fr.xml \
	file://uixml/nilinuxrt.firewall_enable.const.ja.xml \
	file://uixml/nilinuxrt.firewall_enable.const.ko.xml \
	file://uixml/nilinuxrt.firewall_enable.const.xml \
	file://uixml/nilinuxrt.firewall_enable.const.zh-CN.xml \
	file://uixml/nilinuxrt.firewall_enable.def.xml \
"

FILES:${PN}-firewall = "\
	${settingsdatadir}/firewall_enable.ini \
	${uixmldir}/nilinuxrt.firewall_enable.* \
"

RDEPENDS:${PN}-firewall += "sysconfig-settings firewalld"

pkg_postinst_ontarget:${PN}-firewall () {
	ln -sf ${settingsdatadir}/firewall_enable.ini ${systemsettingsdir}/firewall_enable.ini
}

# Leave firewall.enabled as set; firewalld keeps honoring it after removal.
pkg_prerm:${PN}-firewall () {
	rm -f $D${systemsettingsdir}/firewall_enable.ini
}

# sysconfig-settings-worldwritable package
PACKAGES += "${PN}-worldwritable"
SUMMARY:${PN}-worldwritable = "System configuration files to enable automount and world-writable devices"
DESCRIPTION:${PN}-worldwritable = "Configuration files that allow NI hardware configuration applications to enable block device automounting and world-writable device permissions (SystemSettings worldwritable.enabled)."

SRC_URI:append = "\
	file://systemsettings/worldwritable_enable.ini \
	file://uixml/nilinuxrt.worldwritable_enable.binding.xml \
	file://uixml/nilinuxrt.worldwritable_enable.const.de.xml \
	file://uixml/nilinuxrt.worldwritable_enable.const.fr.xml \
	file://uixml/nilinuxrt.worldwritable_enable.const.ja.xml \
	file://uixml/nilinuxrt.worldwritable_enable.const.ko.xml \
	file://uixml/nilinuxrt.worldwritable_enable.const.xml \
	file://uixml/nilinuxrt.worldwritable_enable.const.zh-CN.xml \
	file://uixml/nilinuxrt.worldwritable_enable.def.xml \
"

FILES:${PN}-worldwritable = "\
	${settingsdatadir}/worldwritable_enable.ini \
	${uixmldir}/nilinuxrt.worldwritable_enable.* \
"

RDEPENDS:${PN}-worldwritable += "sysconfig-settings udev-extraconf"

pkg_postinst_ontarget:${PN}-worldwritable () {
	ln -sf ${settingsdatadir}/worldwritable_enable.ini ${systemsettingsdir}/worldwritable_enable.ini
}

# Leave worldwritable.enabled as set; udev keeps honoring it after removal.
pkg_prerm:${PN}-worldwritable () {
	rm -f $D${systemsettingsdir}/worldwritable_enable.ini
}
