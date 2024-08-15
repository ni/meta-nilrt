SUMMARY = "Open-source package dependencies of NI proprietary and internal products."
LICENSE = "MIT"

# Packagegroups which include recipes that dynamically rename their packages -
# like libxkbcommon - may not use allarch.
# https://www.mail-archive.com/openembedded-core@lists.openembedded.org/msg155223.html
PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup


# NI-RFSA/G
# Contact: Dharaniprakash Kurdimath <dharaniprakash.kurdimath@ni.com>
RDEPENDS:${PN} += "\
	tbb \
"

# nissl and nissleay
# Contact: Rich Tollerton <rich.tollerton@ni.com>
RDEPENDS:${PN} += "\
	apache-websocket \
	apache2 \
	apr-iconv \
"

# ni-sync
#RDEPENDS:${PN} += "\
#	ni-grpc-device \
#"

# Required components for Veristand.
# Engineering contact: Marcelo Izaguirre
RDEPENDS:${PN} += "\
	libfmi \
"

# Required for VCOM Toolkit
# Contact: Stefano Caiola <stefano.caiola@ni.com>
RDEPENDS:${PN}:append:x64 = "\
	qtbase \
"

# ni-rdma, libnirdma
# Contact: Eric Gross <eric.gross@ni.com>
RDEPENDS:${PN} += "\
	rdma-core \
"

# ni-flexrio-integratedio-libs, required for csi2serdesconfig
# Contact: Michael Strain <michael.strain@ni.com>
RDEPENDS:${PN}:append:x64 = "\
	python3-core \
	python3-ctypes \
	python3-threading \
"

# NI Test Systems Software
# Contact: Christian Gutierrez <christian.gutierrez@ni.com>
RDEPENDS:${PN} += "\
	libxml-parser-perl \
"

# Required for a mobilize step that installs a specific Python version
# and requires building Python on the test system
# Contact: ulf.glaeser@ni.com
# Team: DAQ.SW.Ops@ni.com
RDEPENDS:${PN} += "\
	lz4 \
"

# Required by nisetupscripts for pre-installer testing
# Contact: Zach Hindes <zach.hindes@ni.com>
RDEPENDS:${PN}:append:x64 = "\
	ruby \
"

# Required by aim-arinc-429
# Maintainer: AIM GmbH
# Contact: Karl Grosz
RDEPENDS:${PN} += "\
	coreutils \
	g++ \
	g++-symlinks \
	gcc \
	glibc \
	libnl \
	make \
	pkgconfig \
"

# Required by PXIPS ATS events
# Team: PXI PS
# Contact: Kevin Khai-Wern Lim
RDEPENDS:${PN} += "\
	memtester \
	ntfs-3g-ntfsprogs \
"

# Required by aim-arinc-664
# Maintainer: AIM GmbH
# Contact: Michael Tillerson <michael.tillerson@ni.com>
RDEPENDS:${PN} += "\
	libpcap \
"

# Required by LabVIEW Real-Time
# Team: LabVIEW Real-Time
# Contact: sharpk
RDEPENDS:${PN} += "\
	libglu \
"
# Required by PAtools Runtimesystem
# Team: Transportation BU - Modern Battery Lab
# Contact: Deborah Bryant
RDEPENDS:${PN} += "\
	libxslt \
	samba \
	ntp \
	ntp-tickadj \
	ntp-utils \
"
