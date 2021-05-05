SUMMARY = "gRPC server providing remote access to NI device driver APIs."
DESCRIPTION = ""
HOMEPAGE = "https://github.com/ni/grpc-device"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	https://github.com/ni/grpc-device/releases/download/${${PN}_RELEASE_TAG}/ni-grpc-device-server-ni-linux-rt-x64.tar.gz;name=release-tar;downloadfilename=${PN}_${${PN}_RELEASE_TAG}.tar.gz;subdir=${PN}-server \
	https://github.com/ni/grpc-device/releases/download/${${PN}_RELEASE_TAG}/ni-grpc-device-client.tar.gz;name=release-client-tar;downloadfilename=${PN}-client_${PN}_${${PN}_RELEASE_TAG}.tar.gz;subdir=${PN}-client \
"

SRC_URI[release-tar.md5sum] = "200de2f8b1cbc4c26af42cfaed1b01ea"
SRC_URI[release-tar.sha256sum] = "32e0767b28f71a3024921904aa7bcd879cf9e9f2a98661722ac549446bca0aed"
SRC_URI[release-client-tar.md5sum] = "11da718d6be42848a1e4155e56bdb641"
SRC_URI[release-client-tar.sha256sum] = "0555fc1f2867b4ed9314354ed87041b7065365a24ddaf3526c1871f519df34ad"

${PN}_RELEASE_TAG ?= "v${PV}"
${PN}_RELEASE_TAG = "v${PV}-rc1"

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
