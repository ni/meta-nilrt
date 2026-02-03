do_deploy:class-target:append() {
    install --mode 0644 -t ${DEPLOYDIR}/ \
        ${WORKDIR}/ovmf/ovmf.code.fd \
        ${WORKDIR}/ovmf/ovmf.vars.fd
}
