FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/udev:"

SRC_URI =+ "file://0001-ni-skip-kmod-for-wl12xx.patch"

EXTRA_OECONF =+ "enable_mtd_probe=no"
