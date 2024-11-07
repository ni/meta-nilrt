# (C) Copyright 2013,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Runmode specific packages for NI Linux Realtime distribution"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "\
	cpp \
	cpp-symlinks \
	dosfstools \
	e2fsprogs \
	e2fsprogs-e2fsck \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	gdbserver \
	glibc-gconv-cp932 \
	glibc-gconv-cp936 \
	glibc-gconv-iso8859-1 \
	iproute2-tc \
	libmpc \
	libpython3 \
	librtpi \
	libyaml \
	linux-firmware-radeon \
	lldpd \
	mpfr \
	nftables \
	ni-configpersistentlogs \
	ni-locale-alias \
	ni-modules-autoload \
	niwatchdogpet \
	opkg-utils-shell-tools \
	parted \
	python3-aiodns \
	python3-aiohttp \
	python3-asn1crypto \
	python3-async-timeout \
	python3-asyncio \
	python3-attrs \
	python3-avahi \
	python3-certifi \
	python3-cffi \
	python3-chardet \
	python3-codecs \
	python3-compile \
	python3-compression \
	python3-configparser \
	python3-core \
	python3-crypt \
	python3-cryptography \
	python3-ctypes \
	python3-datetime \
	python3-dateutil \
	python3-dbus \
	python3-difflib \
	python3-email \
	python3-fcntl \
	python3-html \
	python3-idna \
	python3-idna-ssl \
	python3-io \
	python3-jinja2 \
	python3-json \
	python3-logging \
	python3-markupsafe \
	python3-math \
	python3-mime \
	python3-misc \
	python3-mmap \
	python3-modules \
	python3-msgpack \
	python3-multidict \
	python3-multiprocessing \
	python3-ndg-httpsclient \
	python3-netclient \
	python3-netserver \
	python3-numbers \
	python3-pickle \
	python3-pika \
	python3-pkgutil \
	python3-plistlib \
	python3-ply \
	python3-pprint \
	python3-profile \
	python3-psutil \
	python3-pyasn1 \
	python3-pycares \
	python3-pycparser \
	python3-pycryptodome \
	python3-pyiface \
	python3-pyinotify \
	python3-pyopenssl \
	python3-pyroute2 \
	python3-pysocks \
	python3-pyyaml \
	python3-requests \
	python3-resource \
	python3-setuptools \
	python3-shell \
	python3-six \
	python3-stringold \
	python3-terminal \
	python3-threading \
	python3-tornado \
	python3-typing-extensions \
	python3-unittest \
	python3-unixadmin \
	python3-urllib3 \
	python3-xml \
	python3-xmlrpc \
	python3-yarl \
	rtctl \
	salt-common \
	salt-minion \
	sysconfig-settings \
	systemimageupdateinfo \
	util-linux-sfdisk \
	vlan \
	zip \
"

# Required components for Veristand.
# Engineering contact: Marcelo Izaguirre
RDEPENDS:${PN}:append = "\
	libfmi-dev \
"
