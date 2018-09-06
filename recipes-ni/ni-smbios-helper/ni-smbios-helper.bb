SUMMARY = "Smbios helper"
DESCRIPTION = "Bios helper support files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://smbios_helper \
        file://efi_helper \
"

RDEPENDS_${PN} = "bash"

S = "${WORKDIR}"

do_install () {
     install -d ${D}${datadir}/nisysinfo
     install -m 0755    ${WORKDIR}/smbios_helper    ${D}${datadir}/nisysinfo/
     install -m 0755    ${WORKDIR}/efi_helper    ${D}${datadir}/nisysinfo/
}

FILES_${PN} += "\
	${datadir}/nisysinfo/smbios_helper \
    ${datadir}/nisysinfo/efi_helper \
"