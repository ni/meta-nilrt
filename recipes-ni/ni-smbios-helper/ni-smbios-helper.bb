SUMMARY = "SMBIOS helper"
DESCRIPTION = "BIOS helper support files"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.1"


SRC_URI = "\
	file://efi_helper \
	file://smbios_helper \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${datadir}/nisysinfo
	install -m 0755 ${S}/efi_helper    ${D}${datadir}/nisysinfo/
	install -m 0755 ${S}/smbios_helper ${D}${datadir}/nisysinfo/
}


FILES:${PN} += "${datadir}/nisysinfo/*"
# efi_helper rdeps: efivar
# smbios_helper rdeps: dmidecode
RDEPENDS:${PN} = "\
	bash \
	dmidecode \
	efivar \
"
