PACKAGES =+ "${PN}-hostname ${PN}-ls ${PN}-chcon ${PN}-shred ${PN}-timeout"

FILES:${PN}-hostname = "${base_bindir}/hostname.${PN}"
FILES:${PN}-ls = "${base_bindir}/ls.${PN}"
FILES:${PN}-chcon = "${bindir}/chcon.${PN}"
FILES:${PN}-shred = "${bindir}/shred.${PN}"
FILES:${PN}-timeout = "${bindir}/timeout.${PN}"

DEPENDS += "shadow-native pseudo-native niacctbase"

RDEPENDS:${PN}-hostname += "niacctbase"

# preserve existing behavior when installing base package
RDEPENDS:coreutils:class-target += "\
	${PN}-hostname \
	${PN}-ls \
	${PN}-chcon \
	${PN}-shred \
	${PN}-timeout \
"

PRIORITY = "120"

pkg_postinst:${PN}-hostname () {
	chmod 4550 $D${base_bindir}/hostname.${BPN}
	chown 0:${LVRT_GROUP} $D${base_bindir}/hostname.${BPN}

	update-alternatives --install ${base_bindir}/hostname hostname hostname.${BPN} 100
}

pkg_prerm:${PN}-hostname () {
	update-alternatives --remove hostname hostname.${BPN}
}

pkg_postinst:${PN}-ls () {
	update-alternatives --install ${base_bindir}/ls ls ls.${BPN} ${PRIORITY}
}

pkg_prerm:${PN}-ls () {
	update-alternatives --remove ls ls.${BPN}
}

pkg_postinst:${PN}-chcon () {
	update-alternatives --install ${bindir}/chcon chcon chcon.${BPN} ${PRIORITY}
}

pkg_prerm:${PN}-chcon () {
	update-alternatives --remove chcon chcon.${BPN}
}

pkg_postinst:${PN}-shred () {
	update-alternatives --install ${bindir}/shred shred shred.${BPN} ${PRIORITY}
}

pkg_prerm:${PN}-shred () {
	update-alternatives --remove shred shred.${BPN}
}

pkg_postinst:${PN}-timeout () {
	update-alternatives --install ${bindir}/timeout timeout timeout.${BPN} ${PRIORITY}
}

pkg_prerm:${PN}-timeout () {
	update-alternatives --remove timeout timeout.${BPN}
}
