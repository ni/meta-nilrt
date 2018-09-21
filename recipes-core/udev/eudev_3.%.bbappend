FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/udev:"

SRC_URI =+ " \
	file://0001-ni-skip-kmod-for-wl12xx.patch \
"

EXTRA_OECONF =+ "--disable-mtd_probe"

# we don't use the hwdb and it consumes around 10mb of space so disable it
PACKAGECONFIG_remove += "hwdb"

# in NILRT we use another network interface naming scheme than the eudev
# default (network interfaces are renamed via initscripts)
do_install_append() {
	rm ${D}${sysconfdir}/udev/rules.d/80-net-name-slot.rules
}
