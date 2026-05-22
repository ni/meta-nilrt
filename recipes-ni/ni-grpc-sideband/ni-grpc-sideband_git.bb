SUMMARY = "NI gRPC Sideband"
DESCRIPTION = "Library for high-performance sideband data transfers with NI gRPC Device Server."
HOMEPAGE = "https://github.com/ni/grpc-sideband"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=75f4e5c7ec4f89925cd35ff3952beafa"


PV = "0.1.0+git${SRCPV}"


SRC_URI = "git://github.com/ni/grpc-sideband.git;protocol=https;nobranch=1 \
	file://0001-Revert-update-function-to-accept-boolean-trigger-11.patch \
	file://0002-CMakeLists-optionally-diable-submodule-dependencies.patch \
	file://0003-CMakeLists-version-project-0.1.0.patch \
	file://0006-CMakeLists-install-headers-to-namespaced-subdir.patch \
"
SRCREV = "0ce928851df2e335ebdc385cced6d46a662c505e"


inherit cmake

EXTRA_OECMAKE += "\
	-DINCLUDE_SIDEBAND_RDMA=OFF \
	-DSIDEBAND_STATIC=OFF \
	-DUSE_SUBMODULE_DEPENDENCIES=OFF \
	-DCMAKE_BUILD_TYPE=Release \
"
