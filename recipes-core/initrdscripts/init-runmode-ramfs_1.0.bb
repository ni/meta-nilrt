SUMMARY = "Init script used for booting older NILRT runmode initramfs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
	file://init-runmode-ramfs.sh	\
"

DEPENDS = "bash busybox util-linux nilrtdiskcrypt"
RDEPENDS_${PN} += "\
	bash \
	busybox \
	util-linux-lsblk \
	util-linux-switch-root \
	util-linux-mount \
	nilrtdiskcrypt-open \
	nilrtdiskcrypt-reseal \
"

do_install() {
	install -m 0755 ${WORKDIR}/init-runmode-ramfs.sh ${D}/init
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} += "/init"
