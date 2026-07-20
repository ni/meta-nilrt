SUMMARY = "NI Linux RT safemode static nftables firewall"
DESCRIPTION = "Default-deny nftables ruleset and SysV init script that \
permit only the minimal set of services expected while a target is in \
safemode. Uses the nft userspace tool only (no daemon, no Python), so it \
is suitable for the size-constrained safemode initramfs."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

S = "${UNPACKDIR}"

SRC_URI = "\
	file://ni-safemode-firewall \
	file://safemode.nft \
"

FILES:${PN} = "\
	${sysconfdir}/init.d/ni-safemode-firewall \
	${sysconfdir}/nftables/safemode.nft \
"

RDEPENDS:${PN} += "nftables"

INITSCRIPT_NAME = "ni-safemode-firewall"
INITSCRIPT_PARAMS = "start 39 S ."

inherit update-rc.d

do_install () {
	install -d ${D}${sysconfdir}/init.d
	install -d ${D}${sysconfdir}/nftables
	install -m 0755 ${S}/ni-safemode-firewall ${D}${sysconfdir}/init.d
	install -m 0644 ${S}/safemode.nft         ${D}${sysconfdir}/nftables
}
