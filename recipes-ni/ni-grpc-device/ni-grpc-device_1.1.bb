SUMMARY = "NI gRPC Device Server"
DESCRIPTION = "gRPC server providing remote access to NI device driver APIs."
HOMEPAGE = "https://github.com/ni/grpc-device"
SECTION = "base"
LICENSE = "MIT & Apache-2.0"
LIC_FILES_CHKSUM = "\
	file://${PN}-server/LICENSE;md5=08ed4de411f83eee0363c9d12e31e92d \
	file://${PN}-server/ThirdPartyNotices.txt;md5=6def9ca42f278e76d89243699fae2b67 \
"

SRC_URI = "\
	https://github.com/ni/grpc-device/releases/download/${RELEASE_TAG}/ni-grpc-device-server-ni-linux-rt-x64.tar.gz;name=release-server-tar;downloadfilename=${PN}_${RELEASE_TAG}.tar.gz;subdir=${PN}-server \
	https://github.com/ni/grpc-device/releases/download/${RELEASE_TAG}/ni-grpc-device-client.tar.gz;name=release-client-tar;downloadfilename=${PN}-client_${PN}_${RELEASE_TAG}.tar.gz;subdir=${PN}-client \
	file://enumerate-device.py \
	file://run-ptest \
	file://session_pb2_grpc.py \
	file://session_pb2.py \
"

SRC_URI[release-server-tar.md5sum] = "27d8b2976e6253a2bcbee6330ffc24cf"
SRC_URI[release-server-tar.sha256sum] = "e68feaeaf6f9a3a3be3fd572c0d213531958dcd3ca8d33ccce57093b64f2747f"
SRC_URI[release-client-tar.md5sum] = "9e14d407c530825c80e7a39507a59b08"
SRC_URI[release-client-tar.sha256sum] = "4a8bdd02821301ebf41945943efdfc25439e30c43a62b3ec42dd5feccdbdf868"

PV = "1.1.0"
RELEASE_TAG = "v1.1.0-rc1"

S = "${WORKDIR}"

do_install_append () {
	install -d ${D}${bindir}
	install --mode=0755 --owner=0 --group=0 ${S}/${PN}-server/ni_grpc_device_server ${D}${bindir}

	install -d ${D}${datadir}/${BPN}
	install --mode=0644 --owner=0 --group=0 ${S}/${PN}-server/server_config.json ${D}${datadir}/${BPN}/server_config.json.example

	# package .proto files from the -client archive for use by developers
	install -d ${D}${includedir}/${PN}
	install --mode 644 --owner=0 --group=0 ${S}/${PN}-client/proto/*.proto ${D}${includedir}/${PN}/
}

inherit ptest

RDEPENDS_${PN}-ptest += "${PN} bash python3-grpcio"

do_install_ptest_append () {
	install -d ${D}${PTEST_PATH}
	install -m 0644 ${S}/enumerate-device.py ${D}${PTEST_PATH}/
	install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}/
	install -m 0644 ${S}/session_pb2_grpc.py ${D}${PTEST_PATH}/
	install -m 0644 ${S}/session_pb2.py ${D}${PTEST_PATH}/
}
