SUMMARY = "Configuration for NILRT Logging"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SRC_URI = "file://logging_paths.ini"

FILES:${PN} += "${datadir}/ni-resetniconfig"

do_install() {
    install -d ${D}${datadir}/ni-resetniconfig
    install -m 0644 ${UNPACKDIR}/logging_paths.ini ${D}${datadir}/ni-resetniconfig/logging_paths.ini
}
