SUMMARY = "NI gRPC Device Server"
DESCRIPTION = "gRPC server providing remote access to NI device driver APIs."
HOMEPAGE = "https://github.com/ni/grpc-device"
SECTION = "base"
LICENSE = "MIT & Apache-2.0"
LIC_FILES_CHKSUM = "\
	file://LICENSE;md5=08ed4de411f83eee0363c9d12e31e92d \
	file://ThirdPartyNotices.txt;md5=6def9ca42f278e76d89243699fae2b67 \
"

DEPENDS += "\
	grpc-native \
	nlohmann-json-native \
	protobuf-native \
	python3-mako-native \
	python3-native \
"

BPV = "1.1"
PV = "${BPV}+git${SRCPV}"

SRC_URI = " \
	git://github.com/ni/grpc-device.git;name=grpc-device;branch=${SRCBRANCH} \
	file://0001-CMakeLists.txt-search-for-host-tools-when-cross-comp.patch \
	file://0002-CMakeLists-only-link-win64-on-windows.patch \
	file://0003-CMakeLists-fixup-RPATH.patch \
	file://0004-CMakeLists.txt-remove-local-protobuf-includes.patch \
"

SRCBRANCH = "main"
SRCREV_grpc-device = "${AUTOREV}"

SRCREV_FORMAT = "grpc-device"

S = "${WORKDIR}/git"


inherit cmake python3native

EXTRA_OECMAKE += "-DCMAKE_BUILD_TYPE=Release"

OECMAKE_TARGET_COMPILE = "ni_grpc_device_server"


# grpc-device does not provide an 'install' target (yet); so overwrite the whole
# of do_install, to keep cmake from building anything further.
do_install () {
	# install server binaries
	install -d ${D}${bindir}
	install --mode=0755 ${B}/ni_grpc_device_server ${D}${bindir}

	# install default server_config
	install -d ${D}${datadir}/${BPN}
	install --mode=0644 ${B}/server_config.json ${D}${datadir}/${BPN}/server_config.json.example

	# package .proto files for use by developers
	install -d ${D}${includedir}/${BPN}
	install --mode=0644 ${S}/generated/**/*.proto          ${D}${includedir}/${BPN}
	install --mode=0644 ${S}/source/protobuf/session.proto ${D}${includedir}/${BPN}
}

RDEPENDS_${PN} += "\
	grpc \
	protobuf \
"
