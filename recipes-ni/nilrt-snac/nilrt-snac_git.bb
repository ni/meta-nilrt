SUMMARY = "NILRT SNAC Configuration Tool"
DESCRIPTION = "\
A utility for admins to put a NILRT system into the SNAC configuration.\
"
HOMEPAGE = "https://github.com/ni/nilrt-snac"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=380df876633ca23587b9851600778cc0"


SRC_URI = "\
	git://github.com/ni/nilrt-snac;branch=master;protocol=https \
"


SRCREV = "${AUTOREV}"
PV = "0.1.1+git${SRCPV}"

S = "${WORKDIR}/git"


do_install() {
	oe_runmake install \
		DESTDIR=${D}
}


RDEPENDS:${PN} = "\
	bash \
	opkg \
	python3-core \
"
