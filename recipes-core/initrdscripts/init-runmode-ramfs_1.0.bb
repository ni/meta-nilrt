SUMMARY = "Init script used for booting older NILRT runmode initramfs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
	file://init-runmode-ramfs.sh	\
"

DEPENDS = "bash busybox util-linux nilrtdiskcrypt ${PREFERRED_PROVIDER_virtual/kernel}"

RDEPENDS_${PN} += "\
	bash \
	busybox \
	util-linux-lsblk \
	util-linux-switch-root \
	util-linux-mount \
	nilrtdiskcrypt-open \
	nilrtdiskcrypt-reseal \
	kernel-module-tpm-tis \
	kernel-module-dm-crypt \
"

do_install() {
	install -m 0755 ${WORKDIR}/init-runmode-ramfs.sh ${D}/init
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} += "/init"
