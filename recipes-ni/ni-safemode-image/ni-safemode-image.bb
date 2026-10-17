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

# Track the safemode image version so opkg can order upgrades between bundles.
PV = "${DISTRO_VERSION}"

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
# opkg is the single version authority here, so -f bypasses niinstallsafemode's
# own version check and makes reinstalling the same version idempotent instead
# of failing the postinst.
pkg_postinst_ontarget:${PN}() {
	/usr/local/natinst/bin/niinstallsafemode ${datadir}/nilrt-safemode/safemode.cfg -f
}
