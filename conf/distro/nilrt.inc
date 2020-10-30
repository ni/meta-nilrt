MAINTAINER = "NI Linux Real-Time Maintainers <nilrt@ni.com>"

TARGET_VENDOR = "-nilrt"

GRUB_BRANCH = "nilrt/19.0"

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

# Clean *most* package work directories
INHERIT += " rm_work "
RM_WORK_EXCLUDE += " \
	dist-nilrt-efi-ab \
	dist-nilrt-efi-ab-gateway \
	dist-nilrt-grub-gateway \
	lvcomms-restore-mode-image \
	niconsole-image \
	niconsole-image-safe \
	restore-mode-image \
	safemode-restore-image \
"

LVRT_USER = "lvuser"
LVRT_GROUP = "ni"

SDK_VENDOR = "-nilrtsdk"
SDK_VERSION := "${@'${DISTRO_VERSION}'}"

LOCALCONF_VERSION = "1"
LAYER_CONF_VERSION ?= "7"

VIRTUAL-RUNTIME_graphical_init_manager = "xserver-xfce-init"

VIRTUAL-RUNTIME_base-utils-hwclock = "util-linux-hwclock"

VIRTUAL-RUNTIME_initscripts = "initscripts"

VIRTUAL-RUNTIME_init_manager = "sysvinit"

# For qemu images
VIRTUAL-RUNTIME_keymaps = ""

PREFERRED_PROVIDER_virtual/xserver ?= "xserver-xorg"

PREFERRED_PROVIDER_virtual/libgl ?= "mesa"
PREFERRED_PROVIDER_virtual/libgles1 ?= "mesa"
PREFERRED_PROVIDER_virtual/libgles2 ?= "mesa"

PREFERRED_PROVIDER_virtual/kernel ?= "linux-nilrt"

PREFERRED_RPROVIDER_java2-runtime = "openjdk-8"

PREFERRED_RPROVIDER_refpolicy = "refpolicy-standard"

PREFERRED_RPROVIDER_polarssl = "polarssl"

PREFERRED_RPROVIDER_initd-functions = "initscripts"

PREFERRED_PROVIDER_jpeg = "jpeg"
PREFERRED_PROVIDER_jpeg-native = "jpeg-native"

PREFERRED_PROVIDER_getopt = "busybox"
VIRTUAL-RUNTIME_getopt = "busybox"

OLDEST_KERNEL = "3.14.3"

QTDEMOS = "quicky fotowall"

EFI_PROVIDER = "grub-efi"

INCLUDE_PYCS = "0"

DISTRO_FEATURES_BACKFILL_CONSIDERED_arm = "gobject-introspection-data"

# touchsreen calibration doesn't work with xf86-input-libinput, only with xf86-input-edev
BAD_RECOMMENDATIONS += "xf86-input-libinput"

# Do not "--enable-udev_sync" in lvm2/libdevmapper
LVM2_PACKAGECONFIG_remove_class-target = "udev"
RRECOMMENDS_libdevmapper_remove_class-target = "lvm2-udevrules"

# these bbappends are constantly stale and since we don't use them
# mask to avoid the bitbake warning noise
BBMASK += "meta-cloud-services/meta-openstack/recipes-kernel"

# Base URI to NI Linux RT's Git repository
NILRT_GIT ?= "git://github.com/ni"

# Creates ``*-lic`` subpackages for all OE recipes if enabled
LICENSE_CREATE_PACKAGE ?= "1"

# Create Packages.filelist for each feed
PACKAGE_ENABLE_FILELIST ?= "True"


## OPKG SOURCE FEEDS ##

# Root URI of all NILRT feeds
NILRT_FEEDS_URI                    ?= "http://download.ni.com/ni-linux-rt/feeds"
NILRT_FEEDS_URI_RELEASE            ?= "${NILRT_FEEDS_URI}/${NILRT_RELEASE_NAME}"
NILRT_MACHINE_FEED_URI_x64         ?= "${NILRT_FEEDS_URI_RELEASE}/x64"
NILRT_MACHINE_FEED_URI_xilinx-zynq ?= "${NILRT_FEEDS_URI_RELEASE}/arm"

NILRT_MAIN_FEED_VERSION            ?= "2020.6"
NILRT_MACHINE_FEED_URI_nimain      ?= "${NILRT_FEEDS_URI}/${NILRT_MAIN_FEED_VERSION}/ni-main"

# OE Packages with TUNEARCHes
PACKAGE_FEED_URIS       = "${NILRT_MACHINE_FEED_URI}"
PACKAGE_FEED_BASE_PATHS = "extra main"
PACKAGE_FEED_ARCHS      = "all ${MACHINE} ${TUNE_PKGARCH}"

# NI Packages, which are flat feeds
# Add additional (non-constructed) opkg sources with the pattern:
# PACKAGE_FEED_URIS_ADDITIONAL[source_name] = "source_uri"
PACKAGE_FEED_URIS_ADDITIONAL = 'fake value for PACKAGE_FEED_URIS_ADDITIONAL'

# NI distribution packages
PACKAGE_FEED_URIS_ADDITIONAL[NI-dist] = "${NILRT_FEEDS_URI}/dist"

# NI-main packages
PACKAGE_FEED_URIS_ADDITIONAL[ni-main] = "${NILRT_MACHINE_FEED_URI_nimain}"

BUILD_IMAGES_FROM_FEEDS = "1"

# Get NIOE packages from local deploy feed and NI packages from web feed
NILRT_LOCAL_FEED_URI ?= "file://${DEPLOY_DIR_IPK}"

# Configure feed URIs for image builds
IPK_FEED_URIS += "\
    NIOE-all##${NILRT_LOCAL_FEED_URI}/all \
    NIOE-${MACHINE}##${NILRT_LOCAL_FEED_URI}/${MACHINE} \
    NIOE-${TUNE_PKGARCH}##${NILRT_LOCAL_FEED_URI}/${TUNE_PKGARCH} \
    NI-software##${NILRT_MACHINE_FEED_URI_nimain} \
"
