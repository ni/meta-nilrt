SUMMARY = "Init script used for booting older NILRT runmode initramfs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
	file://init-runmode-ramfs.sh	\
"

RDEPENDS_${PN} += "bash"

do_install() {
	install -m 0755 ${WORKDIR}/init-runmode-ramfs.sh ${D}/init
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} += "/init"
