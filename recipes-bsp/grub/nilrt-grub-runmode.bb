SUMMARY = "nilrt runmode grub configuration"
DESCRIPTION = "nilrt distro-specific runmode boot files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"
S = "${UNPACKDIR}"

FILESEXTRAPATHS:prepend := "${THISDIR}/grub:"

SRC_URI += " \
    file://grub-runmode-bootimage.cfg \
    file://grub.d \
"

FILES:${PN}     += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg /boot/runmode/cpu-mitigations.cfg.disable"
CONFFILES:${PN} += "/boot/runmode/bootimage.cfg /boot/runmode/bootimage.cfg.d/*.cfg /boot/runmode/cpu-mitigations.cfg.disable"

do_install () {
	install -d ${D}/boot/runmode
	install -m 0644 ${UNPACKDIR}/grub-runmode-bootimage.cfg ${D}/boot/runmode/bootimage.cfg
	# CPU vulnerability mitigations are enabled by default. Removing the
	# '.disable' suffix from this file disables them.
	install -m 0600 ${UNPACKDIR}/grub.d/cpu-mitigations.cfg ${D}/boot/runmode/cpu-mitigations.cfg.disable
}

# Installing or upgrading this package changes the runmode boot artifacts
# measured into PCR 15 (e.g. an upgrade removes an active cpu-mitigations.cfg),
# so reseal the LUKS keys of encrypted targets to the new boot configuration.
pkg_postinst:${PN} () {
	[ -n "$D" ] && exit 0
	[ -x /usr/sbin/ni-reseal-luks ] || exit 0
	if blkid --label niconfig-luks >/dev/null 2>&1 \
		|| blkid --label nirootfs-luks >/dev/null 2>&1; then
		if ! /usr/sbin/ni-reseal-luks --yes; then
			echo "ERROR: Failed to reseal LUKS keys. Run 'ni-reseal-luks' successfully before restarting, or the encrypted partitions will not unlock." >&2
			exit 1
		fi
	fi
}
