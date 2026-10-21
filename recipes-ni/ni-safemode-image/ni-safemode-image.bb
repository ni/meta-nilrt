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
# b=2, rc=3, f=4), e.g. 27.0.0d23 -> 27.0.0.0.23.
def safemode_pkg_version(d):
    import re
    buildname = d.getVar('BUILDNAME') or ''
    m = re.match(r'([0-9]+)\.([0-9]+)(?:\.([0-9]+)(?:(d|a|b|rc|f)([0-9]+))?)?', buildname)
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

# Pull in the safemode rootfs archive that this package wraps.
do_install[depends] += "${SAFEMODE_IMAGE}:do_image_complete"

do_install() {
	install -d ${D}${SAFEMODE_BUNDLE_DIR}
	install -m 0644 "${DEPLOY_DIR_IMAGE}/${SAFEMODE_IMAGE}-${MACHINE}.tar.gz" \
		"${D}${SAFEMODE_BUNDLE_DIR}/${SAFEMODE_BUNDLE}"
}

FILES:${PN} = "${SAFEMODE_BUNDLE_DIR}"

# niinstallsafemode (from ni-safemode-utils) applies the bundle; fw-printenv is
# used by niinstallsafemode to read the target DeviceCode.
RDEPENDS:${PN} += "ni-safemode-utils fw-printenv"

# Apply the bundled safemode image on the target only, never during rootfs
# assembly on the build host. niinstallsafemode writes /boot/.safe, which is
# mounted in both runmode and safemode, so this upgrade works from either mode.
# It is run without -f so its own version check refuses downgrades; the same
# exit-code handling as the System Configuration firmware update is used.
pkg_postinst_ontarget:${PN}() {
	rc=0
	/usr/local/natinst/bin/niinstallsafemode ${datadir}/nilrt-safemode/safemode.cfg || rc=$?
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
}
