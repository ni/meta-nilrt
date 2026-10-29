SUMMARY = "NILRT safemode boot image, packaged for opkg feed installation"
DESCRIPTION = "Packages the NILRT safemode boot image (kernel, ramdisk and grub \
configuration) as an opkg-installable bundle. Installing or upgrading this \
package applies the contained safemode image to the boot partition via \
niinstallsafemode, letting a device upgrade safemode from a package feed while \
running in either runmode or an older safemode on the target - without MAX or \
the Hardware Configuration utility."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

# The safemode image is machine specific (kernel and device codes), so this
# package must be machine specific too. This also declares the arch
# compatibility opkg uses when selecting the bundle for a target.
PACKAGE_ARCH = "${MACHINE_ARCH}"

# Version the package by the safemode it carries (BUILDNAME is stamped into
# bootimage.ini as component_version), mapped so that opkg orders it the same
# way nicompareversion does: the NI phase letter becomes a number (d=0, a=1,
# b=2, rc=3, f=4), e.g. 27.0.0d23 -> 27.0.0.0.23. The whole BUILDNAME must be
# an NI version, optionally followed by the -x64 suffix CI builds carry.
def safemode_pkg_version(d):
    import re
    buildname = d.getVar('BUILDNAME') or ''
    m = re.fullmatch(r'([0-9]+)\.([0-9]+)(?:\.([0-9]+)(?:(d|a|b|rc|f)([0-9]+))?)?(?:-x64)?', buildname)
    if not m:
        bb.fatal("ni-safemode-image: BUILDNAME '%s' is not an NI version such as 27.0.0d23" % buildname)
    major, minor, sub, phase, build = m.groups()
    phases = {'d': 0, 'a': 1, 'b': 2, 'rc': 3, 'f': 4}
    return '.'.join(str(int(x)) for x in (major, minor, sub or 0, phases[phase or 'f'], build or 0))

PV = "${@safemode_pkg_version(d)}"

# The safemode rootfs archive is currently produced for x64 only. arm safemode
# is delivered as a FIT image through a different path.
COMPATIBLE_MACHINE = "x64"

SAFEMODE_IMAGE = "nilrt-safemode-rootfs"
SAFEMODE_BUNDLE_DIR = "${datadir}/nilrt-safemode"
SAFEMODE_BUNDLE = "safemode.cfg"
# ni-shutdown-guard holds off reboot while either file exists: the lock for the
# duration of the postinst, the marker until a pending reseal has succeeded.
SAFEMODE_INSTALL_LOCK = "/run/${PN}.lock"
SAFEMODE_RESEAL_PENDING = "${localstatedir}/lib/${PN}/reseal-pending"

# Pull in the safemode rootfs archive that this package wraps.
do_install[depends] += "${SAFEMODE_IMAGE}:do_image_complete"

do_install() {
	install -d ${D}${SAFEMODE_BUNDLE_DIR}
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${SAFEMODE_IMAGE}-${MACHINE}.tar.gz" \
		"${D}${SAFEMODE_BUNDLE_DIR}/${SAFEMODE_BUNDLE}"
	install -d ${D}${sysconfdir}/holdoff-shutdown.d
	printf '%s\n' "-f ${SAFEMODE_INSTALL_LOCK} -f ${SAFEMODE_RESEAL_PENDING}" > ${D}${sysconfdir}/holdoff-shutdown.d/${PN}
}

FILES:${PN} = "${SAFEMODE_BUNDLE_DIR} ${sysconfdir}/holdoff-shutdown.d/${PN}"

# niinstallsafemode (from ni-safemode-utils) applies the bundle; fw-printenv is
# used by niinstallsafemode to read the target DeviceCode.
RDEPENDS:${PN} += "ni-safemode-utils fw-printenv ni-shutdown-guard"

# Apply the bundled safemode image on the target only, never during rootfs
# assembly on the build host. niinstallsafemode writes /boot/.safe, which is
# mounted in both runmode and safemode, so this upgrade works from either mode.
# It is run without -f so its own version check refuses downgrades; the same
# exit-code handling as the System Configuration firmware update is used.
# Encrypted volumes are unlocked by TPM policies sealed to the measured safemode
# boot files. Before a newer bundle is applied to an encrypted target, a dry run
# must show the volumes can be resealed, and a pending-reseal marker is kept
# until resealing succeeds, so a retried configure reseals even when the files
# were already copied and reboot stays held off in the meantime.
pkg_postinst_ontarget:${PN}() {
	bundle=${SAFEMODE_BUNDLE_DIR}/${SAFEMODE_BUNDLE}
	pending=${SAFEMODE_RESEAL_PENDING}
	touch ${SAFEMODE_INSTALL_LOCK}
	trap 'rm -f ${SAFEMODE_INSTALL_LOCK}' EXIT

	encrypted=
	if [ -x ${sbindir}/ni-reseal-luks ] && \
	   { ${base_sbindir}/blkid --label niconfig-luks || ${base_sbindir}/blkid --label nirootfs-luks; } >/dev/null 2>&1; then
		encrypted=1
	fi
	if [ -n "$encrypted" ] && [ ! -e "$pending" ]; then
		cur=$(sed -n 's/^component_version=//p' /boot/.safe/bootimage.ini)
		new=$({ tar -xzOf "$bundle" bootimage.ini || tar -xzOf "$bundle" ./bootimage.ini; } 2>/dev/null | \
			sed -n 's/^component_version=//p')
		if [ -z "$new" ]; then
			echo "${PN}: cannot read the safemode version from $bundle; not upgrading safemode" >&2
			exit 1
		fi
		result=
		[ -n "$cur" ] && result=$(/usr/local/natinst/bin/nicompareversion "$cur" "$new")
		case "$result" in
		equal|older)
			;;
		*)
			if ! ${sbindir}/ni-reseal-luks --dry-run --yes >/dev/null; then
				echo "${PN}: the encrypted volumes cannot be resealed; not upgrading safemode" >&2
				exit 1
			fi
			mkdir -p "$(dirname "$pending")"
			touch "$pending"
			;;
		esac
	fi

	rc=0
	/usr/local/natinst/bin/niinstallsafemode "$bundle" || rc=$?
	case $rc in
	0)
		;;
	3)
		echo "${PN}: safemode is already at the packaged version"
		;;
	2)
		echo "${PN}: installed safemode is newer than the packaged version; not downgrading" >&2
		exit 1
		;;
	*)
		echo "${PN}: niinstallsafemode failed with exit code $rc" >&2
		exit 1
		;;
	esac

	if [ -n "$encrypted" ] && [ -e "$pending" ]; then
		if ! ${sbindir}/ni-reseal-luks --yes; then
			echo "${PN}: resealing the encrypted volumes failed; reboot is held off until 'opkg configure ${PN}' reseals them" >&2
			exit 1
		fi
		rm -f "$pending"
	fi
}
