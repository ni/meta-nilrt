BASE_INSTALL ?= "\
	base-files \
	base-passwd \
	busybox \
	netbase \
	${@base_contains("MACHINE_FEATURES", "keyboard", "keymaps", "", d)} \
	"

ANGSTROM_EXTRA_INSTALL ?= "\
	busybox-ifplugd \
	busybox-mdev \
	coreutils-hostname \
	dhcp-client \
	ethtool \
	glibc-gconv-utf-16 \
	initscripts \
	iproute2 \
	iptables \
	libcap-bin \
	libnss-mdns \
	libpam \
	libstdc++ \
	mtd-utils \
	openssh-scp \
	openssh-sftp-server \
	openssh-sftp \
	openssh-ssh \
	openvpn \
	opkg \
	pigz \
	syslog-ng \
	sysvinit \
	tar \
	u-boot-utils \
	util-linux-hwclock \
	"

TZ_DATA_INSTALL ?= "\
	tzdata \
	tzdata-africa \
	tzdata-americas \
	tzdata-asia \
	tzdata-atlantic \
	tzdata-australia \
	tzdata-europe \
	tzdata-misc \
	tzdata-pacific \
	"

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_DEV_MANAGER = "busybox-mdev"

IMAGE_INSTALL = "\
	${BASE_INSTALL} \
	${DISTRO_SSH_DAEMON} \
	${ANGSTROM_EXTRA_INSTALL} \
	${TZ_DATA_INSTALL} \
	"

IMAGE_LINGUAS = ""

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"

require include/niconsole-image.inc

inherit image

