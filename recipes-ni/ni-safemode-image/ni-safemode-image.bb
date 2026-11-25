SUMMARY = "NILRT safemode boot image, packaged for opkg feed installation"
DESCRIPTION = "Packages the NILRT safemode boot image (the kernel, ramdisk \
and grub configuration on x64; the FIT image on arm) as an opkg-installable \
bundle. Installing or upgrading this \
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
# bootimage.ini as component_version on x64 and into the FIT version on arm),
# mapped so that opkg orders it the same
# way nicompareversion does: the NI phase letter becomes a number (d=0, a=1,
# b=2, rc=3, f=4), e.g. 27.0.0d23 -> 27.0.0.0.23. The whole BUILDNAME must be
# an NI version, optionally followed by the machine suffix CI builds carry.
def safemode_pkg_version(d):
    import re
    buildname = d.getVar('BUILDNAME') or ''
    m = re.fullmatch(r'([0-9]+)\.([0-9]+)(?:\.([0-9]+)(?:(d|a|b|rc|f)([0-9]+))?)?(?:-x64|-xilinx-zynq)?', buildname)
    if not m:
        bb.fatal("ni-safemode-image: BUILDNAME '%s' is not an NI version such as 27.0.0d23" % buildname)
    major, minor, sub, phase, build = m.groups()
    phases = {'d': 0, 'a': 1, 'b': 2, 'rc': 3, 'f': 4}
    return '.'.join(str(int(x)) for x in (major, minor, sub or 0, phases[phase or 'f'], build or 0))

PV = "${@safemode_pkg_version(d)}"

COMPATIBLE_MACHINE = "x64|xilinx-zynq"

# x64 ships the safemode rootfs archive niinstallsafemode.x64 installs as is.
# arm ships the FIT image alone: the default FPGA bitfiles niinstallsafemode.arm
# also expects are not built here, so the postinst bundles the target's own.
SAFEMODE_IMAGE = "nilrt-safemode-rootfs"
SAFEMODE_BUNDLE_DIR = "${datadir}/nilrt-safemode"
SAFEMODE_BUNDLE = "safemode.cfg"
SAFEMODE_BUNDLE:xilinx-zynq = "linux_safemode.itb"
SAFEMODE_BUNDLE_SRC = "${DEPLOY_DIR_IMAGE}/${SAFEMODE_IMAGE}-${MACHINE}.tar.gz"
SAFEMODE_BUNDLE_SRC:xilinx-zynq = "${DEPLOY_DIR_IMAGE}/kernel-safemode/linux_safemode.itb"
SAFEMODE_BUNDLE_DEPENDS = "${SAFEMODE_IMAGE}:do_image_complete"
SAFEMODE_BUNDLE_DEPENDS:xilinx-zynq = "linux-nilrt-arm-safemode-fitimage:do_deploy"
SAFEMODE_FORMAT = "tar"
SAFEMODE_FORMAT:xilinx-zynq = "fit"
# Boot files whose change requires resealing encrypted volumes (x64 only).
SAFEMODE_MEASURED = "/boot/.safe/bootimage.cfg /boot/.safe/bzImage /boot/.safe/ramdisk.xz"
SAFEMODE_MEASURED:xilinx-zynq = ""
# ni-shutdown-guard holds off reboot while the lock (for the duration of the
# postinst) or the reseal marker (encrypted volumes still to reseal) exists. A
# copy cannot be retried during shutdown, so the copy journal does not hold it
# off. Both live on the boot partition, outside the measured safemode files, as
# /var/lib is not persistent in safemode.
SAFEMODE_INSTALL_LOCK = "/run/${PN}.lock"
SAFEMODE_STATE_DIR = "/boot/.${PN}"
SAFEMODE_RESEAL_PENDING = "${SAFEMODE_STATE_DIR}/reseal-pending"
SAFEMODE_COPY_PENDING = "${SAFEMODE_STATE_DIR}/copy-pending"
# Copy of the default FPGA bitfile taken before an arm install, relative to the
# runmode root (persistent in both modes; /boot has no room for it).
SAFEMODE_DEFBIT_SAVED = ""
SAFEMODE_DEFBIT_SAVED:xilinx-zynq = "var/lib/${PN}"

# Pull in the safemode image that this package wraps.
do_install[depends] += "${SAFEMODE_BUNDLE_DEPENDS}"

do_install() {
	install -d ${D}${SAFEMODE_BUNDLE_DIR}
	install -m 0644 "${SAFEMODE_BUNDLE_SRC}" "${D}${SAFEMODE_BUNDLE_DIR}/${SAFEMODE_BUNDLE}"
	install -d ${D}${sysconfdir}/holdoff-shutdown.d
	printf '%s\n' "-f ${SAFEMODE_INSTALL_LOCK} -f ${SAFEMODE_RESEAL_PENDING}" > ${D}${sysconfdir}/holdoff-shutdown.d/${PN}
}

FILES:${PN} = "${SAFEMODE_BUNDLE_DIR} ${sysconfdir}/holdoff-shutdown.d/${PN}"

# niinstallsafemode (from ni-safemode-utils) applies the bundle; fw-printenv is
# used by niinstallsafemode to read the target DeviceCode (provided by
# u-boot-tools-fw-utils on arm); blkid (not in BusyBox here) finds encrypted
# volumes.
RDEPENDS:${PN} += "ni-safemode-utils fw-printenv ni-shutdown-guard util-linux-blkid"

# Apply the bundled safemode image on the target only, never during rootfs
# assembly on the build host. niinstallsafemode writes /boot/.safe, which is
# mounted in both runmode and safemode, so this upgrade works from either mode.
# opkg decides which version is installed, so any bundle that differs from the
# installed safemode is applied with niinstallsafemode -f, downgrades included;
# the same version is left in place. On arm the version is read from the FIT
# image, and the image is installed together with the target's current default
# FPGA bitfile, which niinstallsafemode.arm requires in the bundle. That
# bitfile is overwritten in place by the copy, so it is saved first and the
# copy journal names the saved copy, which every retry bundles.
# Encrypted volumes are unlocked by TPM policies sealed to the measured safemode
# boot files (x64). Before a bundle is applied to an encrypted target, a dry run
# must show each volume can be resealed. A copy journal (bundle digest, version
# and volumes to reseal) is kept until the copy completes, then hands over to a
# reseal marker kept until resealing completes, so a retried configure or
# reinstall finishes exactly that work; reboot is held off while the marker exists.
pkg_postinst_ontarget:${PN}() {
	bundle=${SAFEMODE_BUNDLE_DIR}/${SAFEMODE_BUNDLE}
	pending=${SAFEMODE_RESEAL_PENDING}
	journal=${SAFEMODE_COPY_PENDING}
	measured="${SAFEMODE_MEASURED}"
	installer=/usr/local/natinst/bin/niinstallsafemode
	work=
	# Persistent storage: runmode's / is /mnt/userfs in safemode (/tmp is RAM-backed).
	[ -e /etc/natinst/safemode ] && userfs=/mnt/userfs || userfs=
	saved=${SAFEMODE_DEFBIT_SAVED}
	touch ${SAFEMODE_INSTALL_LOCK}
	trap 'rm -f ${SAFEMODE_INSTALL_LOCK}; [ -z "$work" ] || rm -rf "$work"' EXIT

	fit_version() {
		v=$(/sbin/fdtview -l / version "$1" 2>/dev/null) || return 0
		v="${v##*= \"}"
		echo "${v%\"}"
	}

	apply_bundle() {
		if [ "${SAFEMODE_FORMAT}" = tar ]; then
			$installer "$bundle" -f
			return
		fi
		defbit=$(sed -n 's/^defbit=//p' "$journal")
		bits=$(cd "$userfs/$defbit" 2>/dev/null && ls default.bit.* 2>/dev/null)
		if [ -z "$defbit" ] || [ -z "$bits" ]; then
			echo "${PN}: the saved default bitfile is missing" >&2
			return 1
		fi
		# A fixed name, so a retry after a reset replaces the abandoned archive.
		work="$userfs/$defbit/bundle"
		rm -rf "$work"
		mkdir "$work" && \
			tar -cf "$work/safemode.cfg" -C ${SAFEMODE_BUNDLE_DIR} ${SAFEMODE_BUNDLE} -C "$userfs/$defbit" $bits || return 1
		r=0
		# niinstallsafemode.arm backs up the old image and bitfile to userfs and only
		# removes the backup on exit, so a reset leaves one behind; drop those first.
		rm -rf "$userfs"/old-safemode.??????
		$installer "$work/safemode.cfg" -f || r=$?
		rm -rf "$work"
		work=
		# Same ownership as a MAX safemode install, so the web server can report the version.
		[ $r -ne 0 ] || chown -R webserv:ni /boot/.safe || echo "${PN}: cannot change the owner of /boot/.safe" >&2
		return $r
	}

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

	drop_journal() {
		rm -f "$journal"
		[ -z "$saved" ] || rm -rf "$userfs/$saved"
	}

	finish_copy() {
		labels=$(sed -n 's/^reseal=//p' "$journal")
		if [ -n "$labels" ] && ! { printf '%s\n' $labels > "$pending.tmp" && mv -f "$pending.tmp" "$pending" && sync; }; then
			echo "${PN}: cannot write $pending; run 'opkg configure ${PN}' to retry" >&2
			return 1
		fi
		drop_journal
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
		apply_bundle || rc=$?
		if [ $rc -ne 0 ]; then
			echo "${PN}: niinstallsafemode failed with exit code $rc; run 'opkg configure ${PN}' to complete the copy" >&2
			exit 1
		fi
		finish_copy || exit 1
	fi
	if [ -e "$pending" ]; then
		reseal_pending || exit 1
	fi

	if [ "${SAFEMODE_FORMAT}" = fit ]; then
		cur=$(fit_version /boot/.safe/${SAFEMODE_BUNDLE})
		new=$(fit_version "$bundle")
	else
		cur=$(sed -n 's/^component_version=//p' /boot/.safe/bootimage.ini 2>/dev/null || :)
		new=$({ tar -xzOf "$bundle" bootimage.ini || tar -xzOf "$bundle" ./bootimage.ini; } 2>/dev/null | \
			sed -n 's/^component_version=//p')
	fi
	if [ -z "$new" ]; then
		echo "${PN}: cannot read the safemode version from $bundle; not upgrading safemode" >&2
		exit 1
	fi
	result=
	[ -n "$cur" ] && result=$(/usr/local/natinst/bin/nicompareversion "$cur" "$new")
	if [ "$result" = equal ]; then
		echo "${PN}: safemode is already at the packaged version"
		exit 0
	fi

	if [ -n "$luks" ]; then
		if [ ! -x ${sbindir}/ni-reseal-luks ]; then
			echo "${PN}: encrypted volumes found but ni-reseal-luks is not installed; not changing safemode" >&2
			exit 1
		fi
		for l in $luks; do
			if ! ${sbindir}/ni-reseal-luks --dry-run --yes --label "$l" >/dev/null; then
				echo "${PN}: $l cannot be resealed; not changing safemode" >&2
				exit 1
			fi
		done
	fi
	# niinstallsafemode.arm overwrites the image in place and needs 200 KiB to spare,
	# both before it starts (checked even when the new image is smaller) and after the copy.
	if [ "${SAFEMODE_FORMAT}" = fit ]; then
		old=0
		[ ! -e /boot/.safe/${SAFEMODE_BUNDLE} ] || old=$(wc -c < /boot/.safe/${SAFEMODE_BUNDLE})
		grow=$(( $(wc -c < "$bundle") - old ))
		[ $grow -gt 0 ] || grow=0
		avail=$(( $(df -Pk /boot | awk 'END { print $4 }') * 1024 ))
		if [ $(( avail - grow )) -lt 204800 ]; then
			echo "${PN}: not enough space on /boot for the new safemode image ($grow more bytes and 204800 to spare needed, $avail available); not changing safemode" >&2
			exit 1
		fi
		bits=$(cd /boot/.defbit 2>/dev/null && ls default.bit.* 2>/dev/null) || bits=
		rm -rf "$userfs/${saved:?}" "$userfs/$saved.tmp"
		if [ -z "$bits" ] || ! { mkdir -p "$userfs/$saved.tmp" && (cd /boot/.defbit && cp -p $bits "$userfs/$saved.tmp/") && \
		     mv "$userfs/$saved.tmp" "$userfs/$saved" && sync; }; then
			rm -rf "$userfs/$saved" "$userfs/$saved.tmp"
			echo "${PN}: cannot save the default bitfile from /boot/.defbit; not changing safemode" >&2
			exit 1
		fi
	fi
	if ! mkdir -p "$(dirname "$journal")" || \
	   ! { printf '%s\n' "bundle=$(md5sum < "$bundle" | cut -d' ' -f1)" "version=$new" "reseal=${luks# }" "defbit=$saved" > "$journal.tmp" && \
	       mv -f "$journal.tmp" "$journal" && sync; }; then
		rm -f "$journal.tmp" 2>/dev/null || :
		drop_journal
		echo "${PN}: cannot write $journal; not changing safemode" >&2
		exit 1
	fi
	# The bitfile counts too: niinstallsafemode.arm restores the image after a failure,
	# but does not check that restoring the bitfile worked.
	boot_files() {
		md5sum /boot/.safe/* /boot/.defbit/* 2>/dev/null || :
	}
	before=$(boot_files)
	before_measured=$(md5sum $measured </dev/null 2>/dev/null || :)

	rc=0
	apply_bundle || rc=$?
	if [ "$(boot_files)" = "$before" ]; then
		drop_journal
	elif [ $rc -eq 0 ]; then
		if [ "$(md5sum $measured </dev/null 2>/dev/null || :)" = "$before_measured" ]; then
			drop_journal
		else
			finish_copy || exit 1
		fi
	fi
	if [ $rc -ne 0 ]; then
		echo "${PN}: niinstallsafemode failed with exit code $rc" >&2
		if [ -e "$journal" ]; then
			echo "${PN}: run 'opkg configure ${PN}' to complete the copy" >&2
		fi
		exit 1
	fi

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
