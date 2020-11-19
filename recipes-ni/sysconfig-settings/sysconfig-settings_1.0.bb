SUMMARY = "System configuration files"
DESCRIPTION = "Configuration files for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://niselectsystemsettings \
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

FILES_${PN} += "/usr/local/natinst/share/uixml/sysconfig/* \
"

DEPENDS += "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} += "niacctbase bash"

do_install[depends] += "niacctbase:do_populate_sysroot"

S = "${WORKDIR}"

do_install () {
	# UIXML config (soft dip switches, etc.)
	install -d -m 0755 ${D}/usr/local/natinst/share/uixml/sysconfig/
	install -m 0644 ${WORKDIR}/uixml/* ${D}/usr/local/natinst/share/uixml/sysconfig/

	# Common interface for system settings (soft dip switches, etc.)
	install -d -m 0775 ${D}${localstatedir}/local/natinst/systemsettings/
	chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/systemsettings/
	install -m 0644 ${WORKDIR}/systemsettings/* ${D}${localstatedir}/local/natinst/systemsettings/

	install -d -m 0755 ${D}${sysconfdir}/natinst/
	install -m 0755 ${WORKDIR}/niselectsystemsettings ${D}${sysconfdir}/natinst/
}

pkg_postinst_ontarget_${PN} () {
	${sysconfdir}/natinst/niselectsystemsettings postinst
}
