PRINC := "${@int(PRINC) + 1}"

PACKAGES =+ "${PN}-hostname"

EXTRA_OECONF_class-target += "--enable-install-program=hostname"

FILES_${PN}-hostname = "${base_bindir}/hostname.${PN}"

do_install_append() {
	# rename hostname manually since its not part of base_bindir_progs
	mv ${D}${bindir}/hostname ${D}${base_bindir}/hostname.${BPN}
}

pkg_postinst_coreutils-hostname () {
	update-alternatives --install ${base_bindir}/hostname hostname hostname.${BPN} 100
}

pkg_prerm_coreutils-hostname () {
	update-alternatives --remove hostname hostname.${BPN}
}
