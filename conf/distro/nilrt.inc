DISTRO_NAME = "NI Linux Real-Time"

MAINTAINER = "NI Linux Real-Time Maintainers <nilrt@ni.com>"

TARGET_VENDOR = "-nilrt"

# Inherit the default LIBC features superset from OE-core
DISTRO_FEATURES += "${DISTRO_FEATURES_LIBC}"

# Common features enabled on all NILRT flavours
DISTRO_FEATURES += "\
        argp \
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
"

LVRT_USER = "lvuser"
LVRT_GROUP = "ni"

SDK_VENDOR = "-nilrtsdk"
SDK_VERSION := "${@'${DISTRO_VERSION}'}"

LOCALCONF_VERSION = "1"
LAYER_CONF_VERSION ?= "5"

VIRTUAL-RUNTIME_graphical_init_manager = "xserver-xfce-init"

VIRTUAL-RUNTIME_dev_manager ?= "udev"

VIRTUAL-RUNTIME_base-utils-hwclock = "util-linux-hwclock"

# For qemu images
VIRTUAL-RUNTIME_keymaps = ""

PREFERRED_PROVIDER_jpeg = "jpeg"
PREFERRED_PROVIDER_jpeg-native = "jpeg-native"

PREFERRED_VERSION_python-gevent = "1.2.%"
PREFERRED_VERSION_python3-gevent = "1.2.%"

# If SDKMACHINE is not defined, assume toolchain is created
# for a target device and use kernel version 3.14.3 to take
# advantage of ARM optimizations
OLDEST_KERNEL = "${@['3.14.3','2.6.32'][d.getVar('SDKMACHINE', True) != None]}"

QTDEMOS = "quicky fotowall"

GCCVERSION = "6.3%"

DISTRO_FEATURES_BACKFILL_CONSIDERED = "gobject-introspection-data"