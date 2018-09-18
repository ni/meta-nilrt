FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI_append_arm = "\
        file://fw_env.config \
	file://0001-tools-fw_setenv-Increase-script-buffer-size.patch \
	file://0002-fw_printenv-Don-t-bail-out-directly-after-one-env-re.patch \
"

do_install_append(){
    install -m 0644 ${WORKDIR}/fw_env.config ${D}${sysconfdir}
    chown 0:${LVRT_GROUP} ${D}${base_sbindir}/fw_printenv
    chmod 4550 ${D}${base_sbindir}/fw_printenv
    ln -sf ${base_sbindir}/fw_printenv ${D}${base_sbindir}/fw_setenv
}
