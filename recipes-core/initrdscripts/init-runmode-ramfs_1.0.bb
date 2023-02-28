SUMMARY = "Init script for booting the NILRT runmode initramfs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	file://init-runmode-ramfs.sh	\
"

DEPENDS = "bash busybox util-linux ${PREFERRED_PROVIDER_virtual/kernel}"

RDEPENDS:${PN} += "\
	bash \
	busybox \
	util-linux-lsblk \
	util-linux-switch-root \
	util-linux-mount \
"

do_install() {
	install -m 0755 ${WORKDIR}/init-runmode-ramfs.sh ${D}/init
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} += "/init"

