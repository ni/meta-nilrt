SUMMARY = "NI gRPC Sideband"
DESCRIPTION = "Library for high-performance sideband data transfers with NI gRPC Device Server."
HOMEPAGE = "https://github.com/ni/grpc-sideband"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=75f4e5c7ec4f89925cd35ff3952beafa"

DEPENDS += "\
	grpc \
"

SRC_URI = "git://github.com/ni/grpc-sideband.git;protocol=https;nobranch=1"
SRCREV = "e351b75f2df9d932fb7993520429d7c680031864"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE += "\
	-DINCLUDE_SIDEBAND_RDMA=OFF \
	-DSIDEBAND_STATIC=OFF \
	-DCMAKE_BUILD_TYPE=Release \
"

do_install:append() {
	# grpc-sideband has no cmake install() targets; copy the shared library manually
	install -d ${D}${libdir}
	install --mode=0755 ${B}/libni_grpc_sideband.so ${D}${libdir}/libni_grpc_sideband.so
}

FILES:${PN} += "${libdir}/libni_grpc_sideband.so"
FILES:${PN}-dev += "${libdir}/libni_grpc_sideband.so"

# The .so has no SONAME so it lands in -dev; suppress the resulting QA warnings
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dev += "dev-elf"
