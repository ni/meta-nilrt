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
# ni-shutdown-guard holds off reboot while the lock (for the duration of the
# postinst) or the reseal marker (encrypted volumes still to reseal) exists. A
# copy cannot be retried during shutdown, so the copy journal does not hold it
# off. Both live on the boot partition, outside the measured safemode files, as
# /var/lib is not persistent in safemode.
SAFEMODE_INSTALL_LOCK = "/run/${PN}.lock"
SAFEMODE_STATE_DIR = "/boot/.${PN}"
SAFEMODE_RESEAL_PENDING = "${SAFEMODE_STATE_DIR}/reseal-pending"
SAFEMODE_COPY_PENDING = "${SAFEMODE_STATE_DIR}/copy-pending"

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
# used by niinstallsafemode to read the target DeviceCode; blkid (not in
# BusyBox here) finds encrypted volumes.
RDEPENDS:${PN} += "ni-safemode-utils fw-printenv ni-shutdown-guard util-linux-blkid"

# Apply the bundled safemode image on the target only, never during rootfs
# assembly on the build host. niinstallsafemode writes /boot/.safe, which is
# mounted in both runmode and safemode, so this upgrade works from either mode.
# It is run without -f so its own version check refuses downgrades; the same
# exit-code handling as the System Configuration firmware update is used.
# Encrypted volumes are unlocked by TPM policies sealed to the measured safemode
# boot files. Before a newer bundle is applied to an encrypted target, a dry run
# must show each volume can be resealed. A copy journal (bundle digest, version
# and volumes to reseal) is kept until the copy completes, then hands over to a
# reseal marker kept until resealing completes, so a retried configure or
# reinstall finishes exactly that work; reboot is held off while the marker exists.
pkg_postinst_ontarget:${PN}() {
	bundle=${SAFEMODE_BUNDLE_DIR}/${SAFEMODE_BUNDLE}
	pending=${SAFEMODE_RESEAL_PENDING}
	journal=${SAFEMODE_COPY_PENDING}
	measured="/boot/.safe/bootimage.cfg /boot/.safe/bzImage /boot/.safe/ramdisk.xz"
	installer=/usr/local/natinst/bin/niinstallsafemode
	touch ${SAFEMODE_INSTALL_LOCK}
	trap 'rm -f ${SAFEMODE_INSTALL_LOCK}' EXIT

	reseal_pending() {
		for l in $(cat "$pending"); do
			if ! ${sbindir}/ni-reseal-luks --yes --label "$l"; then
				echo "${PN}: resealing $l failed; reboot is held off until 'opkg configure ${PN}' reseals it" >&2
				return 1
			fi
			sed -i "/^$l\$/d" "$pending"
		done
		rm -f "$pending"
	}

	finish_copy() {
		labels=$(sed -n 's/^reseal=//p' "$journal")
		if [ -n "$labels" ] && ! { printf '%s\n' $labels > "$pending.tmp" && mv -f "$pending.tmp" "$pending" && sync; }; then
			echo "${PN}: cannot write $pending; run 'opkg configure ${PN}' to retry" >&2
			return 1
		fi
		rm -f "$journal"
	}

	blkid_out=$(${base_sbindir}/blkid 2>/dev/null || :)
	luks=
	for l in niconfig-luks nirootfs-luks; do
		case "$blkid_out" in
		*"LABEL=\"$l\""*) luks="$luks $l" ;;
		esac
	done

	if [ -e "$journal" ]; then
		if ! grep -qx "bundle=$(md5sum < "$bundle" | cut -d' ' -f1)" "$journal"; then
			echo "${PN}: an unfinished upgrade to safemode $(sed -n 's/^version=//p' "$journal") is pending; reinstall that ${PN} version to complete it" >&2
			exit 1
		fi
		rc=0
		$installer "$bundle" -f || rc=$?
		if [ $rc -ne 0 ]; then
			echo "${PN}: niinstallsafemode failed with exit code $rc; run 'opkg configure ${PN}' to complete the copy" >&2
			exit 1
		fi
		finish_copy || exit 1
	fi
	if [ -e "$pending" ]; then
		reseal_pending || exit 1
	fi

	cur=$(sed -n 's/^component_version=//p' /boot/.safe/bootimage.ini 2>/dev/null || :)
	new=$({ tar -xzOf "$bundle" bootimage.ini || tar -xzOf "$bundle" ./bootimage.ini; } 2>/dev/null | \
		sed -n 's/^component_version=//p')
	if [ -z "$new" ]; then
		echo "${PN}: cannot read the safemode version from $bundle; not upgrading safemode" >&2
		exit 1
	fi
	result=
	[ -n "$cur" ] && result=$(/usr/local/natinst/bin/nicompareversion "$cur" "$new")
	tracked=
	case "$result" in
	equal)
		echo "${PN}: safemode is already at the packaged version"
		exit 0
		;;
	older)
		;;
	*)
		if [ -n "$luks" ]; then
			if [ ! -x ${sbindir}/ni-reseal-luks ]; then
				echo "${PN}: encrypted volumes found but ni-reseal-luks is not installed; not upgrading safemode" >&2
				exit 1
			fi
			for l in $luks; do
				if ! ${sbindir}/ni-reseal-luks --dry-run --yes --label "$l" >/dev/null; then
					echo "${PN}: $l cannot be resealed; not upgrading safemode" >&2
					exit 1
				fi
			done
		fi
		if ! mkdir -p "$(dirname "$journal")" || \
		   ! { printf '%s\n' "bundle=$(md5sum < "$bundle" | cut -d' ' -f1)" "version=$new" "reseal=${luks# }" > "$journal.tmp" && \
		       mv -f "$journal.tmp" "$journal" && sync; }; then
			rm -f "$journal" "$journal.tmp" 2>/dev/null || :
			echo "${PN}: cannot write $journal; not upgrading safemode" >&2
			exit 1
		fi
		tracked=1
		before=$(md5sum /boot/.safe/* 2>/dev/null || :)
		before_measured=$(md5sum $measured 2>/dev/null || :)
		;;
	esac

	rc=0
	$installer "$bundle" || rc=$?
	if [ -n "$tracked" ]; then
		if [ "$(md5sum /boot/.safe/* 2>/dev/null || :)" = "$before" ]; then
			rm -f "$journal"
		elif [ $rc -eq 0 ]; then
			if [ "$(md5sum $measured 2>/dev/null || :)" = "$before_measured" ]; then
				rm -f "$journal"
			else
				finish_copy || exit 1
			fi
		fi
	fi
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
		if [ -e "$journal" ]; then
			echo "${PN}: run 'opkg configure ${PN}' to complete the copy" >&2
		fi
		exit 1
		;;
	esac

	if [ -e "$pending" ]; then
		reseal_pending || exit 1
	fi
}

# Removal would take the bundle and the shutdown holdoff with it, so it is
# refused while a copy or reseal is unfinished; upgrades and reinstalls are not.
pkg_prerm:${PN}() {
	if [ "$1" = remove ] && [ -e ${SAFEMODE_COPY_PENDING} -o -e ${SAFEMODE_RESEAL_PENDING} ]; then
		echo "${PN}: a safemode upgrade is unfinished; run 'opkg configure ${PN}' to complete it before removing ${PN}" >&2
		exit 1
	fi
}
