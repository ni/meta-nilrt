SUMMARY = "NILRT initramfs init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	file://init-nilrt-ramfs.sh \
"

RDEPENDS_${PN} += " \
	bash \
	busybox \
	util-linux-lsblk \
	util-linux-switch-root \
	util-linux-mount \
	e2fsprogs-mke2fs \
"

do_install() {
	install -m 0755 ${WORKDIR}/init-nilrt-ramfs.sh ${D}/init
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} += " /init"
