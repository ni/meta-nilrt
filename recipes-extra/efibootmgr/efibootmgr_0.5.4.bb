DESCRIPTION = "Linux user-space application to modify the EFI Boot Manager."
SUMMARY = "EFI Boot Manager"
HOMEPAGE = "http://linux.dell.com/efibootmgr/"
SECTION = "base"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=0636e73ff0215e8d672dc4c32c317bb3"
DEPENDS = "pciutils \
    zlib"
COMPATIBLE_HOST = "(i.86|x86_64|arm|aarch64).*-linux"

PR = "r1"

SRC_URI = "http://linux.dell.com/efibootmgr/permalink/efibootmgr-${PV}.tar.gz \
    file://ldflags.patch \
    file://docbook-fixes.patch \
    file://w-keep-existing-mbr-signature.patch \
    file://efibootmgr-0.5.4-support-4k-sectors.patch \
    file://efibootmgr-0.5.4-Work-around-broken-Apple-firmware.patch \
    file://efibootmgr-0.5.4-Remove-device-path-padding-on-non-Itanium.patch \
    file://efibootmgr-0.5.4-fix-minor-memory-leak.patch \
    file://efibootmgr-0.5.4-fix-disk-minor-number-discovery.patch \
    file://efibootmgr-0.5.4-make_boot_var-does-not-check-for-failed-status-with-.patch"

SRC_URI[md5sum] = "cfcf24752d6461b73f7ba964bbf73169"
SRC_URI[sha256sum] = "b562a47a4f5327494992f2ee6ae14a75c5aeb9b4a3a78a06749d5cd2917b8e71"

S = "${WORKDIR}/efibootmgr-${PV}"

EXTRA_OEMAKE = "'CC=${CC}' 'CFLAGS=${CFLAGS} -I${S}/src/lib -I${S}/src/include'"

do_install () {
    install -D -p -m0755 src/efibootmgr/efibootmgr ${D}/${sbindir}/efibootmgr
}
