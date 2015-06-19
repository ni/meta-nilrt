PACKAGECONFIG_append = " sqlite3"
PACKAGECONFIG[sqlite3] = "--with-sqlite3=${STAGING_DIR_HOST}${prefix},,,"

FILES_${PN}     += "${libdir}/apr-util-1/apr_dbd_sqlite3-1.so"
FILES_${PN}-dev += "${libdir}/apr-util-1/apr_dbd_sqlite3.so* ${libdir}/apr-util-1/apr_dbd_sqlite3.la"
FILES_${PN}-staticdev += "${libdir}/apr-util-1/apr_dbd_sqlite3.a"
