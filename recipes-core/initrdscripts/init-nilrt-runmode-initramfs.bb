SUMMARY = "NILRT runmode initramfs init script"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "core"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"


# ==============================================================================
# RECIPE VARIABLES
# ==============================================================================

DEPENDS = "bash busybox util-linux ${PREFERRED_PROVIDER_virtual/kernel}"

PV = "${DISTRO_VERSION}"


# ==============================================================================
# SOURCE VARIABLES
# ==============================================================================

SRC_URI = "\
	file://halt.sh \
	file://init.sh.in \
	file://kernel-vars.sh \
	file://reboot.sh \
"

S = "${UNPACKDIR}"


# ==============================================================================
# CLASSES
# ==============================================================================

inherit update-alternatives

ALTERNATIVE:${PN} = "halt reboot"
ALTERNATIVE_LINK_NAME[halt] = "${sbindir}/halt"
ALTERNATIVE_LINK_NAME[reboot] = "${sbindir}/reboot"
ALTERNATIVE_TARGET[halt] = "${sbindir}/halt.${PN}"
ALTERNATIVE_TARGET[reboot] = "${sbindir}/reboot.${PN}"


# ==============================================================================
# TASKS
# ==============================================================================

do_compile() {
	# Insert packaging variables into the init script.
	sed <${S}/init.sh.in >${S}/init.sh \
		-e "s|@pkglibdir@|${libdir}/${BPN}|g" \
		-e "s|@sysconfdir@|${sysconfdir}|g" \
		-e "s|@sbindir@|${sbindir}|g"
}

do_install() {
	install -m 0744 ${S}/init.sh ${D}/init
	install -D -m 0744 ${S}/halt.sh ${D}${sbindir}/halt.${BPN}
	install -D -m 0744 ${S}/reboot.sh ${D}${sbindir}/reboot.${BPN}
	install -D -m 0644 ${S}/kernel-vars.sh ${D}${libdir}/${BPN}/kernel-vars.sh
}


# ==============================================================================
# PACKAGING
# ==============================================================================

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} += "/init"
RDEPENDS:${PN} += " \
	bash \
	busybox \
	util-linux-lsblk \
	util-linux-switch-root \
	util-linux-mount \
"
