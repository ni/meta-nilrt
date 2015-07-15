do_install_append () {
        install -d ${D}${datadir}/fonts/X11/TTF/

        for i in *.ttf; do
                mv ${D}${prefix}/share/fonts/ttf/${i} ${D}${prefix}/share/fonts/X11/TTF/${i}
        done
}
