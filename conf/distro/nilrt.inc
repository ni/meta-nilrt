DISTRO_NAME = "NI Linux Real-Time"

MAINTAINER = "NI Linux Real-Time Maintainers <nilrt@ni.com>"

TARGET_VENDOR = "-nilrt"

# Inherit the default LIBC features superset from OE-core
DISTRO_FEATURES += "${DISTRO_FEATURES_LIBC}"

# Common features enabled on all NILRT flavours
DISTRO_FEATURES += "\
        argp \
        efi \
        ext2 \
        largefile \
        usbgadget \
        usbhost \
        wifi \
        xattr \
        zeroconf \
        pci \
        sysvinit \
        pam \
        ptest \
        selinux \
	virtualization \
"

LVRT_USER = "lvuser"
LVRT_GROUP = "ni"

SDK_VENDOR = "-nilrtsdk"
SDK_VERSION := "${@'${DISTRO_VERSION}'}"

LOCALCONF_VERSION = "1"
LAYER_CONF_VERSION ?= "5"

VIRTUAL-RUNTIME_graphical_init_manager = "xserver-xfce-init"

VIRTUAL-RUNTIME_base-utils-hwclock = "util-linux-hwclock"

# For qemu images
VIRTUAL-RUNTIME_keymaps = ""

PREFERRED_PROVIDER_virtual/kernel ?= "linux-nilrt"

PREFERRED_PROVIDER_jpeg = "jpeg"
PREFERRED_PROVIDER_jpeg-native = "jpeg-native"

PREFERRED_PROVIDER_getopt = "busybox"
VIRTUAL-RUNTIME_getopt = "busybox"

OLDEST_KERNEL = "3.14.3"

QTDEMOS = "quicky fotowall"

GCCVERSION = "7.3%"

EFI_PROVIDER = "grub-efi"

DISTRO_FEATURES_BACKFILL_CONSIDERED = "gobject-introspection-data"

# touchsreen calibration doesn't work with xf86-input-libinput, only with xf86-input-edev
BAD_RECOMMENDATIONS += "xf86-input-libinput"

# these bbappends are constantly stale and since we don't use them
# mask to avoid the bitbake warning noise
BBMASK += "meta-cloud-services/meta-openstack/recipes-kernel"
