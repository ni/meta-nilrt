FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI =+ " \
            file://0001-xrdp-Make-init.d-script-compatible-with-nilrt.patch \
            file://0002-sesman-Add-xfce4-and-custom-window-manager-support.patch \
            "

inherit update-rc.d
INITSCRIPT_NAME = "xrdp"
INITSCRIPT_PARAMS = "defaults 25"
