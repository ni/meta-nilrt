SUMMARY = "Miscellaneous nilrt utilities"
DESCRIPTION = "nilrt distro-specific miscellaneous utilities that provide basic system functionality."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "\
	niacctbase \
	update-rc.d-native \
"

PV = "2.0"


SRC_URI = "\
	file://functions.common \
	file://nisetbootmode \
	file://nisetbootmode.functions \
	file://nisetled \
	file://nisetprimarymac \
	file://status_led \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${bindir}
	install -m 0755 ${S}/status_led ${D}${bindir}

	install -d ${D}${sysconfdir}/init.d
	install -m 0550 ${S}/nisetbootmode   ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nisetled        ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/nisetprimarymac ${D}${sysconfdir}/init.d

	install -d ${D}${libdir}
	install -m 0440 ${S}/nisetbootmode.functions ${D}${libdir}

	install -d ${D}${sysconfdir}/natinst/networking
	install -m 0755 ${S}/functions.common ${D}${sysconfdir}/natinst/networking

	update-rc.d -r ${D} nisetled        start 40 S .
	update-rc.d -r ${D} nisetbootmode   start 80 S . stop 0 0 6 .
	update-rc.d -r ${D} nisetprimarymac start 4 5 .

	chown 0:${LVRT_GROUP} ${D}${bindir}/status_led
	chown 0:${LVRT_GROUP} ${D}${sysconfdir}/init.d/nisetbootmode
}


RDEPENDS:${PN} += "niacctbase bash"
RDEPENDS:${PN}:append:x64 += "fw-printenv"

FILES:${PN} += "\
	${bindir}/status_led \
	${libdir}/nisetbootmode.functions \
	${sysconfdir}/init.d/* \
	${sysconfdir}/natinst/networking/functions.common \
"
