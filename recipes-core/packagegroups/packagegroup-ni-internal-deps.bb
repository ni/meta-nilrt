SUMMARY = "Open-source package dependencies of NI proprietary and internal products."
LICENSE = "MIT"

inherit packagegroup

# NI-RFSA/G
# Contact: Dharaniprakash Kurdimath <dharaniprakash.kurdimath@ni.com>
RDEPENDS:${PN} += "\
	tbb \
"

# nissl and nissleay
# Contact: Haris Okanovic <haris.okanovic@ni.com>
RDEPENDS:${PN} += "\
	apache-websocket \
	apache2 \
	apr-iconv \
"

# ni-sync
RDEPENDS:${PN} += "\
	ni-grpc-device \
"

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
