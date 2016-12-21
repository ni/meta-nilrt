FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/udev:"

SRC_URI =+ " \
	file://0001-ni-skip-kmod-for-wl12xx.patch \
	file://0001-configure.ac-add-option-to-disable-mtd_probe.patch \
"

EXTRA_OECONF =+ "--disable-mtd_probe"
