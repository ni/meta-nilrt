PACKAGECONFIG_append = " ldap crypto sqlite3"
PACKAGECONFIG[ldap] = "--with-ldap,,openldap,"
PACKAGECONFIG[crypto] = "--with-crypto,,,"
PACKAGECONFIG[sqlite3] = "--with-sqlite3=${STAGING_DIR_HOST}${prefix},,,"

FILES_${PN}     += "${libdir}/apr-util-1/apr_ldap-1.so"
FILES_${PN}-dev += "${libdir}/apr-util-1/apr_ldap.so* ${libdir}/apr-util-1/apr_ldap.la"
FILES_${PN}-staticdev += "${libdir}/apr-util-1/apr_ldap.a"

FILES_${PN}     += "${libdir}/apr-util-1/apr_crypto_openssl-1.so"
FILES_${PN}-dev += "${libdir}/apr-util-1/apr_crypto_openssl.so* ${libdir}/apr-util-1/apr_crypto_openssl.la"
FILES_${PN}-staticdev += "${libdir}/apr-util-1/apr_crypto_openssl.a"

FILES_${PN}     += "${libdir}/apr-util-1/apr_dbd_sqlite3-1.so"
FILES_${PN}-dev += "${libdir}/apr-util-1/apr_dbd_sqlite3.so* ${libdir}/apr-util-1/apr_dbd_sqlite3.la"
FILES_${PN}-staticdev += "${libdir}/apr-util-1/apr_dbd_sqlite3.a"
