SUMMARY = "NILRT SNAC Configuration Tool"
DESCRIPTION = "\
A utility that helps system administrators place their NI LinuxRT (NILRT) \
devices into a Secured, Network-Attached Configuration (SNAC).\
"
HOMEPAGE = "https://github.com/ni/nilrt-snac"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=380df876633ca23587b9851600778cc0"


SRC_URI = "\
	git://github.com/ni/nilrt-snac;branch=master;protocol=https \
	file://run-ptest \
"

SRCREV = "68f5ab078871e5bbdf98f9804381024481688414"
PV = "3.1.0"

S = "${WORKDIR}/git"


inherit ptest


do_install() {
	oe_runmake install \
		DESTDIR=${D}
	
	install -d ${D}${sysconfdir}/snac
	
	install -m 644 ${D}/${docdir}/${PN}/snac.conf.example ${D}${sysconfdir}/snac/snac.conf
}

do_install_ptest() {
	install -m 0755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
}

CONFFILES:${PN} = "${sysconfdir}/snac/snac.conf"
FILES:${PN} += "\
	${datadir}/* \
"

RDEPENDS:${PN} = "\
	bash \
	opkg \
	python3-core \
"

FILES:${PN}-ptest += "${libdir}/${PN}/tests/integration"
RDEPENDS:${PN}-ptest += "\
	bash \
	python3-core \
	python3-pytest \
	python3-unittest-automake-output \
"
