SUMMARY = "CompactRIO support files"
DESCRIPTION = "CompactRIO miscellaneous support files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = "base"
DEPENDS = "niacctbase update-rc.d-native"
RDEPENDS_${PN} += "niacctbase bash"

SRC_URI_append_x64 = "file://nisetfpgaautoload \
                      file://nisetconsoleout \
"

S = "${WORKDIR}"

group = "${LVRT_GROUP}"

do_install () {
     install -d ${D}${sysconfdir}/init.d/
     if [ "${TARGET_ARCH}" = "x86_64" ]; then
          install -m 0755   ${S}/nisetfpgaautoload    ${D}${sysconfdir}/init.d
          install -m 0550   ${S}/nisetconsoleout      ${D}${sysconfdir}/init.d
          chown 0:${group} ${D}${sysconfdir}/init.d/nisetconsoleout

          update-rc.d -r ${D} nisetfpgaautoload start 81 S . stop 3 0 6 .
          update-rc.d -r ${D} nisetconsoleout start 15 S . stop 85 0 6 .
     fi
}
