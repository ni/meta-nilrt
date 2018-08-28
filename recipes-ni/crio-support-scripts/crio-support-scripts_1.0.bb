SUMMARY = "CompactRIO support files"
DESCRIPTION = "CompactRIO miscellaneous support files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"
DEPENDS = "shadow-native pseudo-native niacctbase update-rc.d-native"
RDEPENDS_${PN} += "niacctbase bash"

SRC_URI_append_x64 = "file://nisetfpgaautoload \
                      file://nisetconsoleout \
"

S = "${WORKDIR}"

do_install () {
     install -d ${D}${sysconfdir}/init.d/
     if [ "${TARGET_ARCH}" = "x86_64" ]; then
          install -m 0755   ${S}/nisetfpgaautoload    ${D}${sysconfdir}/init.d
          install -m 0550   ${S}/nisetconsoleout      ${D}${sysconfdir}/init.d
          chown 0:${LVRT_GROUP} ${D}${sysconfdir}/init.d/nisetconsoleout

          update-rc.d -r ${D} nisetfpgaautoload start 81 S . stop 3 0 6 .
          update-rc.d -r ${D} nisetconsoleout start 15 S . stop 85 0 6 .
     fi
}
