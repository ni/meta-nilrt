do_compile_append() {
    cd "${S}/xxd"
    make
}

do_install_append() {
    install -m 0755 xxd/xxd ${D}/usr/bin/xxd
}
