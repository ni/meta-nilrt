DESCRIPTION = "Central Regulatory Domain Agent"
SECTION = "base"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=07c4f6dea3845b02a18dc00c8c87699c"


DEPENDS = "libnl"
RDEPENDS_${PN} = "\
	       wireless-regdb \
	       udev \
"

S = "${WORKDIR}/git"

SRC_URI = "https://github.com/mcgrof/crda.git;protocol=git;tag=v1.1.3"

CFLAGS_append =" -DCONFIG_LIBNL32 -I${STAGING_INCDIR}/libnl3"
LDFLAGS_append =" -lnl-3 -lnl-genl-3 -lm"

do_compile() {
        ${CC} ${CFLAGS} ${S}/reglib.c ${S}/crda.c -o crda ${LDFLAGS}
}

do_install() {
	install -m 0755 -d ${D}${bindir}
        install -m 0755 ${S}/crda ${D}${bindir}
	install -m 0755 -d ${D}${libdir}/udev/rules.d
	sed 's:$(SBINDIR):${bindir}/:' ${S}/udev/regulatory.rules > ${S}/udev/regulatory.rules.parsed
	install -m 0755 ${S}/udev/regulatory.rules.parsed ${D}${libdir}/udev/rules.d/85-regulatory.rules
}

FILES_${PN} += "${libdir}/udev/rules.d/85-regulatory.rules"
