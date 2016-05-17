FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ " \
    file://0001-efivar-add-variable-writing-support.patch \
    file://0002-efivar-add-decimal-variable-printing-support.patch \
"
