# (C) Copyright 2016,
# National Instruments Corporation.
# All rights reserved.

SUMMARY = "Ptest packages necessary for passing the NILRT functional ptest automated test system"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "ptest-runner"

# ptest packages
RDEPENDS:${PN}:append = "\
	busybox-ptest \
	bzip2-ptest \
	coreutils-ptest \
	docker-functional-tests-ptest \
	e2fsprogs-ptest \
	elfutils-ptest \
	ethtool-ptest \
	flex-ptest \
	gettext-ptest \
	glibc-locale-tests-ptest \
	glibc-tests-ptest \
	kernel-tests-ptest \
	liberror-perl-ptest \
	libxml2-ptest \
	nettle-ptest \
	ni-hw-scripts-ptest \
	ni-test-boot-time-ptest \
	nilrt-snac-ptest \
	opkg-ptest \
	pango-ptest \
	parted-ptest \
	perl-ptest \
	pstore-save-ptest \
	python3-appdirs-ptest \
	python3-atomicwrites-ptest \
	python3-bcrypt-ptest \
	python3-more-itertools-ptest \
	python3-pluggy-ptest \
	python3-pyasn1-ptest \
	python3-pyroute2-ptest \
	python3-pyserial-ptest \
	python3-pytz-ptest \
	python3-wcwidth-ptest \
	rt-tests-ptest \
	run-postinsts-ptest \
	sed-ptest \
	util-linux-ptest \
	xorg-fonts-100dpi-ptest \
	zeromq-ptest \
"
