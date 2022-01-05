SUMMARY = "Open-source package dependencies of NI proprietary and internal products."
LICENSE = "MIT"

inherit packagegroup

# nissl and nissleay
# Contact: Haris Okanovic <haris.okanovic@ni.com>
RDEPENDS_${PN} += "\
	apache-websocket \
	apache2 \
	apr-iconv \
"
