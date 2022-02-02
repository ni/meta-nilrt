SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

# sysconfig-settings package
SRC_URI = "file://niselectsystemsettings \
           file://systemsettings/fpga_target.ini \
           file://systemsettings/rt_target.ini \
           file://systemsettings/target_common.ini \
           file://uixml/nilinuxrt.console_enable.binding.xml \
           file://uixml/nilinuxrt.console_enable.const.xml \
           file://uixml/nilinuxrt.console_enable.const.de.xml \
           file://uixml/nilinuxrt.console_enable.const.fr.xml \
           file://uixml/nilinuxrt.console_enable.const.ja.xml \
           file://uixml/nilinuxrt.console_enable.const.ko.xml \
           file://uixml/nilinuxrt.console_enable.const.zh-CN.xml \
           file://uixml/nilinuxrt.console_enable.def.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.binding.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.de.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.fr.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.ja.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.ko.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.const.zh-CN.xml \
           file://uixml/nilinuxrt.rtprotocol_enable.def.xml \
           file://uixml/nilinuxrt.soft_dip_switch.binding.xml \
           file://uixml/nilinuxrt.soft_dip_switch.const.xml \
           file://uixml/nilinuxrt.soft_dip_switch.const.de.xml \
           file://uixml/nilinuxrt.soft_dip_switch.const.fr.xml \
           file://uixml/nilinuxrt.soft_dip_switch.const.ja.xml \
           file://uixml/nilinuxrt.soft_dip_switch.const.ko.xml \
           file://uixml/nilinuxrt.soft_dip_switch.const.zh-CN.xml \
           file://uixml/nilinuxrt.soft_dip_switch.def.xml \
           file://uixml/nirio.soft_dip_switch.binding.xml \
           file://uixml/nirio.soft_dip_switch.const.xml \
           file://uixml/nirio.soft_dip_switch.const.de.xml \
           file://uixml/nirio.soft_dip_switch.const.fr.xml \
           file://uixml/nirio.soft_dip_switch.const.ja.xml \
           file://uixml/nirio.soft_dip_switch.const.ko.xml \
           file://uixml/nirio.soft_dip_switch.const.zh-CN.xml \
           file://uixml/nirio.soft_dip_switch.def.xml \
"

FILES_${PN} = "/etc/natinst/niselectsystemsettings \
               /var/local/natinst/systemsettings/fpga_target.ini \
               /var/local/natinst/systemsettings/rt_target.ini \
               /var/local/natinst/systemsettings/target_common.ini \
               /usr/local/natinst/share/uixml/sysconfig/nilinuxrt.console_enable.* \
               /usr/local/natinst/share/uixml/sysconfig/nilinuxrt.rtprotocol_enable.* \
               /usr/local/natinst/share/uixml/sysconfig/nilinuxrt.soft_dip_switch.* \
               /usr/local/natinst/share/uixml/sysconfig/nirio.soft_dip_switch.* \
"

DEPENDS += "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} += "niacctbase bash"

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

FILES_${PN}-ssh = "/usr/local/natinst/share/uixml/sysconfig/nilinuxrt.sshd_enable.*"

# sysconfig-settings-ui package
PACKAGES += "${PN}-ui"

SUMMARY_${PN}-ui = "System configuration files to enable UI"
DESCRIPTION_${PN}-ui = "Configuration files to enable UI for the National Instruments System Configuration subsystem."

SRC_URI_append = "file://systemsettings/ui_enable.ini \
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

FILES_${PN}-ui = "/var/local/natinst/systemsettings/ui_enable.ini \
                  /usr/local/natinst/share/uixml/sysconfig/nilinuxrt.System.* \
                  /usr/local/natinst/share/uixml/sysconfig/nilinuxrt.ui_enable.* \
"

RDEPENDS_${PN}-ui += "sysconfig-settings niacctbase"

do_install[depends] += "niacctbase:do_populate_sysroot"

S = "${WORKDIR}"

do_install () {
	# UIXML config (soft dip switches, etc.)
	install -d -m 0755 ${D}/usr/local/natinst/share/uixml/sysconfig/
	install -m 0644 ${S}/uixml/* ${D}/usr/local/natinst/share/uixml/sysconfig/

	# Common interface for system settings (soft dip switches, etc.)
	install -d -m 0775 ${D}${localstatedir}/local/natinst/systemsettings/
	chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/systemsettings/
	install -m 0644 ${S}/systemsettings/* ${D}${localstatedir}/local/natinst/systemsettings/

	install -d -m 0755 ${D}${sysconfdir}/natinst/
	install -m 0755 ${S}/niselectsystemsettings ${D}${sysconfdir}/natinst/
}

pkg_postinst_ontarget_${PN} () {
	# Add the memory reservation for Base
	/usr/local/natinst/bin/nirtcfg -s section=RtLinuxMemReserve,token=Base,value=24

	${sysconfdir}/natinst/niselectsystemsettings postinst
}
