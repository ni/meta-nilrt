DESCRIPTION = "Customized settings for the Xfce desktop environment."
SECTION = "x11"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r1"
S = "${WORKDIR}"

DEPENDS = "niacctbase"
RDEPENDS_${PN} = "xfce4-settings xfce4-session xfce4-panel"

user = "${LVRT_USER}"
group = "${LVRT_GROUP}"
homedir = "/home/${user}"
confdir = "${homedir}/.config"
backgrounddir = "/usr/share/backgrounds/xfce"

SRC_URI = "file://autostart/dpms.desktop \
	file://autostart/screensaver.desktop \
	file://menus/xfce-applications.menu \
	file://xfce4/desktop/icons.screen0-624x384.rc \
	file://xfce4/desktop/icons.screen0-624x464.rc \
	file://xfce4/desktop/nilrt-desktop-background.jpg \
	file://xfce4/panel/launcher-1/file_manager_launcher.desktop \
	file://xfce4/panel/launcher-2/terminal_emulator_launcher.desktop \
	file://xfce4/panel/launcher-3/settings_manager_launcher.desktop \
	file://xfce4/panel/launcher-4/display_settings_launcher.desktop \
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
	    ${confdir}/autostart/screensaver.desktop \
	    ${confdir}/menus/xfce-applications.menu \
	    ${confdir}/xfce4/desktop/icons.screen0-624x384.rc \
	    ${confdir}/xfce4/desktop/icons.screen0-624x464.rc \
	    ${backgrounddir}/nilrt-desktop-background.jpg \
	    ${confdir}/xfce4/panel/launcher-1/file_manager_launcher.desktop \
	    ${confdir}/xfce4/panel/launcher-2/terminal_emulator_launcher.desktop \
	    ${confdir}/xfce4/panel/launcher-3/settings_manager_launcher.desktop \
	    ${confdir}/xfce4/panel/launcher-4/display_settings_launcher.desktop \
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

	   install -m 0644 ${S}/autostart/dpms.desktop ${D}${confdir}/autostart/
	   install -m 0644 ${S}/autostart/screensaver.desktop ${D}${confdir}/autostart/
	   install -m 0644 ${S}/menus/xfce-applications.menu ${D}${confdir}/menus/
	   install -m 0644 ${S}/xfce4/desktop/icons.screen0-624x384.rc ${D}${confdir}/xfce4/desktop/
	   install -m 0644 ${S}/xfce4/desktop/icons.screen0-624x464.rc ${D}${confdir}/xfce4/desktop/
	   install -m 0644 ${S}/xfce4/desktop/nilrt-desktop-background.jpg ${D}${backgrounddir}/
	   install -m 0644 ${S}/xfce4/panel/launcher-1/file_manager_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-1/
	   install -m 0644 ${S}/xfce4/panel/launcher-2/terminal_emulator_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-2/
	   install -m 0644 ${S}/xfce4/panel/launcher-3/settings_manager_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-3/
	   install -m 0644 ${S}/xfce4/panel/launcher-4/display_settings_launcher.desktop ${D}${confdir}/xfce4/panel/launcher-4/
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

	   chown -R ${user}:${group} ${D}${homedir}
}
