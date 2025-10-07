FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:xilinx-zynq = " file://${UBOOT_ENV_BINARY} "
