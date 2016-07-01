FILESEXTRAPATHS_prepend := "${THISDIR}/grub-legacy:"

SRC_URI += " \
    file://0001-Define-SMBIOS3-entry-point-structures-for-EFI.patch \
    file://0002-Add-a-module-for-retrieving-SMBIOS-information.patch \
"

do_configure_prepend() {
    ( cd ${S}
      ${S}/autogen.sh )
}
