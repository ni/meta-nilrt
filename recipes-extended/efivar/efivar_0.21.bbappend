FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI =+ "file://add_write_support.patch \
	   file://add_dec_print.patch"
