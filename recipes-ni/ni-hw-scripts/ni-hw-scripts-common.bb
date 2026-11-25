SUMMARY = "Support scripts and utilities common to all NI hardware products"
DESCRIPTION = "Support scripts and utilities for all NI hardware products which are supported by NI LinuxRT."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "\
	update-rc.d-native \
"

SRC_URI += "\
	file://init.d/ni-rename-ifaces \
	file://init.d/nisetserialnumber \
"

S = "${UNPACKDIR}"


inherit allarch
PACKAGE_ARCH = "all"
PACKAGES:remove = "${PN}-staticdev ${PN}-dev ${PN}-dbg"


do_install () {
	install -d ${D}${sysconfdir}/init.d/

	install -m 0755 ${S}/init.d/ni-rename-ifaces     ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/init.d/nisetserialnumber    ${D}${sysconfdir}/init.d
}

pkg_postinst:${PN} () {
	if [ -n "$D" ]; then
		OPT="-r $D"
	else
		OPT="-s"
	fi

	# Rename before sysctl drop-ins (S34) and /etc/sysctl.conf (S36).
	# Migrate the standard existing link when upgrading an installed system.
	if [ -L "$D${sysconfdir}/rcS.d/S38ni-rename-ifaces" ] && \
	   [ "$(readlink "$D${sysconfdir}/rcS.d/S38ni-rename-ifaces")" = "../init.d/ni-rename-ifaces" ] && \
	   [ ! -e "$D${sysconfdir}/rcS.d/S33ni-rename-ifaces" ] && \
	   [ ! -L "$D${sysconfdir}/rcS.d/S33ni-rename-ifaces" ]; then
		if ! mv "$D${sysconfdir}/rcS.d/S38ni-rename-ifaces" \
		        "$D${sysconfdir}/rcS.d/S33ni-rename-ifaces"; then
			echo "ni-hw-scripts-common: failed to migrate ni-rename-ifaces startup link" >&2
			exit 1
		fi
	fi
	update-rc.d $OPT ni-rename-ifaces    start 33 S .
	update-rc.d $OPT nisetserialnumber   start 38 S .
}

pkg_postrm:${PN} () {
	if [ -n "$D" ]; then
		OPT="-f -r $D"
	else
		OPT="-f"
	fi

	update-rc.d $OPT ni-rename-ifaces  remove
	update-rc.d $OPT nisetserialnumber remove
}


FILES:${PN} += "\
	${sysconfdir}/init.d/ni-rename-ifaces \
	${sysconfdir}/init.d/nisetserialnumber \
"

RDEPENDS:${PN} += "\
	bash \
"
