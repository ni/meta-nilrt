DESCRIPTION = "Customized settings for the Xfce desktop environment."
SECTION = "x11"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
S = "${WORKDIR}"

DEPENDS = "shadow-native pseudo-native niacctbase"
RDEPENDS_${PN} = "bash niacctbase xfce4-settings xfce4-session xfce4-panel"

user = "${LVRT_USER}"
group = "${LVRT_GROUP}"
homedir = "/home/${user}"
confdir = "${homedir}/.config"
backgrounddir = "/usr/share/backgrounds/xfce"

SRC_URI = "file://autostart/dpms.desktop \
	file://autostart/dualmonitor.desktop \
	file://autostart/screensaver.desktop \
	file://dual-monitor-setup.sh \
	file://menus/xfce-applications.menu \
	file://xfce4/desktop/icons.screen0-624x384.rc \
	file://xfce4/desktop/icons.screen0-624x464.rc \
	file://xfce4/desktop/nilrt-desktop-background.jpg \
	file://xfce4/panel/launcher-1/file_manager_launcher.desktop \
	file://xfce4/panel/launcher-2/terminal_emulator_launcher.desktop \
	file://xfce4/panel/launcher-3/settings_manager_launcher.desktop \
	file://xfce4/panel/launcher-4/display_settings_launcher.desktop \
	file://xfce4/panel/showpanel \
	file://xfce4/panel/hidepanel \
	file://xfce4/xfconf/xfce-perchannel-xml/keyboards.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/thunar.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-desktop.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-keyboard-shortcuts.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-notifyd.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-panel.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-session.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-settings-editor.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfce4-settings-manager.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xfwm4.xml \
	file://xfce4/xfconf/xfce-perchannel-xml/xsettings.xml \
	"

FILES_${PN} = "${confdir}/autostart/dpms.desktop \
	    ${confdir}/autostart/dualmonitor.desktop \
	    ${confdir}/autostart/screensaver.desktop \
	    ${confdir}/menus/xfce-applications.menu \
	    ${confdir}/xfce4/desktop/icons.screen0-624x384.rc \
	    ${confdir}/xfce4/desktop/icons.screen0-624x464.rc \
	    ${backgrounddir}/nilrt-desktop-background.jpg \
	    ${confdir}/xfce4/panel/launcher-1/file_manager_launcher.desktop \
	    ${confdir}/xfce4/panel/launcher-2/terminal_emulator_launcher.desktop \
	    ${confdir}/xfce4/panel/launcher-3/settings_manager_launcher.desktop \
	    ${confdir}/xfce4/panel/launcher-4/display_settings_launcher.desktop \
	    /usr/local/natinst/bin/showpanel \
	    /usr/local/natinst/bin/hidepanel \
	    /usr/local/bin/showpanel \
	    /usr/local/bin/hidepanel \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/keyboards.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/thunar.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-desktop.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-keyboard-shortcuts.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-notifyd.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-panel.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-session.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-settings-editor.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfce4-settings-manager.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xfwm4.xml \
	    ${confdir}/xfce4/xfconf/xfce-perchannel-xml/xsettings.xml \
	    /usr/local/natinst/bin/dual-monitor-setup.sh \
	    "

do_install () {
	   install -d ${D}${confdir}/autostart
	   install -d ${D}${confdir}/menus
	   install -d ${D}${confdir}/xfce4/desktop
	   install -d ${D}${confdir}/xfce4/panel/launcher-1
	   install -d ${D}${confdir}/xfce4/panel/launcher-2
	   install -d ${D}${confdir}/xfce4/panel/launcher-3
	   install -d ${D}${confdir}/xfce4/panel/launcher-4
	   install -d ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml
	   install -d ${D}${backgrounddir}
	   install -d ${D}/usr/local/bin
	   install -d ${D}/usr/local/natinst/bin

	   install -m 0644 ${S}/autostart/dpms.desktop ${D}${confdir}/autostart/
	   install -m 0644 ${S}/autostart/dualmonitor.desktop ${D}${confdir}/autostart/
	   install -m 0644 ${S}/autostart/screensaver.desktop ${D}${confdir}/autostart/
	   install -m 0644 ${S}/menus/xfce-applications.menu ${D}${confdir}/menus/
	   install -m 0644 ${S}/xfce4/desktop/icons.screen0-624x384.rc ${D}${confdir}/xfce4/desktop/
	   install -m 0644 ${S}/xfce4/desktop/icons.screen0-624x464.rc ${D}${confdir}/xfce4/desktop/
	   install -m 0644 ${S}/xfce4/desktop/nilrt-desktop-background.jpg ${D}${backgrounddir}/
	   install -m 0644 ${S}/xfce4/panel/launcher-1/file_manager_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-1/
	   install -m 0644 ${S}/xfce4/panel/launcher-2/terminal_emulator_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-2/
	   install -m 0644 ${S}/xfce4/panel/launcher-3/settings_manager_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-3/
	   install -m 0644 ${S}/xfce4/panel/launcher-4/display_settings_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-4/
	   install -m 0755 ${S}/xfce4/panel/showpanel ${D}/usr/local/natinst/bin/
	   install -m 0755 ${S}/xfce4/panel/hidepanel ${D}/usr/local/natinst/bin/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/keyboards.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/thunar.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-desktop.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-keyboard-shortcuts.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-notifyd.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-panel.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-session.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-settings-editor.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfce4-settings-manager.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xfwm4.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0644 ${S}/xfce4/xfconf/xfce-perchannel-xml/xsettings.xml ${D}${confdir}/xfce4/xfconf/xfce-perchannel-xml/
	   install -m 0755 ${S}/dual-monitor-setup.sh ${D}/usr/local/natinst/bin

	   ln -sf /usr/local/natinst/bin/showpanel ${D}/usr/local/bin/showpanel
	   ln -sf /usr/local/natinst/bin/hidepanel ${D}/usr/local/bin/hidepanel

	   chown -R ${user}:${group} ${D}${homedir}
}
