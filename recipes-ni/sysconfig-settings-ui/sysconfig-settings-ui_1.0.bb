SUMMARY = "System configuration files to enable UI"
DESCRIPTION = "Configuration files to enable UI for the National Instruments System Configuration subsystem."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = " \
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
	   file://systemsettings/ui_enable.ini \
"

FILES_${PN} += "/usr/local/natinst/share/uixml/sysconfig/* \
"

DEPENDS = "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} = "sysconfig-settings niacctbase"

S = "${WORKDIR}"

do_install () {
	# UIXML config (soft dip switches, etc.)
	install -d ${D}/usr/local/natinst/share/uixml/sysconfig/
	install -m 0644 ${WORKDIR}/uixml/* ${D}/usr/local/natinst/share/uixml/sysconfig/

	# Interface for enabling UI for System Configuration
	install -d -m 0775 ${D}${localstatedir}/local/natinst/systemsettings/
	chown ${LVRT_USER}:${LVRT_GROUP} ${D}${localstatedir}/local/natinst/systemsettings/
	install -m 0644 ${WORKDIR}/systemsettings/ui_enable.ini ${D}${localstatedir}/local/natinst/systemsettings/
}

