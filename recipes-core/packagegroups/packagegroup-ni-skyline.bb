# (C) Copyright 2024,
#  National Instruments Corporation.
#  All rights reserved.

SUMMARY = "Open Source package dependencies for the NI Skyline software stack."
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = "\
	salt-common \
	salt-minion \
"

# Miscellaneous python deps for closed-source Skyline components
RDEPENDS:${PN}:append = "\
	python3-aiodns \
	python3-aiohttp \
	python3-avahi \
	python3-configparser \
	python3-difflib \
	python3-misc \
	python3-mmap \
	python3-multiprocessing \
	python3-pika \
	python3-profile \
	python3-psutil \
	python3-pyiface \
	python3-pyinotify \
	python3-pyroute2 \
	python3-resource \
	python3-terminal \
	python3-unixadmin \
	python3-xmlrpc \
"
