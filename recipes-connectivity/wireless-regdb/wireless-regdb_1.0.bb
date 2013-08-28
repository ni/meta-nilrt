DESCRIPTION = "Wireless Regulatory Database"
SECTION = "base"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=07c4f6dea3845b02a18dc00c8c87699c"

PROVIDES += "wireless-regdb"

PR = "r0"

S = "${WORKDIR}/git"

SRC_URI = "git://git.kernel.org/pub/scm/linux/kernel/git/linville/wireless-regdb.git;protocol=git;tag=master-2013-02-13"

FILES_${PN} += "${libdir}/crda/regulatory.bin"
FILES_${PN} += "${libdir}/crda/pubkeys/linville.key.pub.pem"

do_configure() {
        :
}

do_compile () {
        :
}

do_install() {
	install -m 0755 -d ${D}${libdir}/crda ${D}${libdir}/crda/pubkeys
	install -m 0644 ${S}/regulatory.bin ${D}${libdir}/crda
	install -m 0644 ${S}/linville.key.pub.pem ${D}${libdir}/crda/pubkeys
}