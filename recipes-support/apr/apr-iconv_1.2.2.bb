SUMMARY = "Apache Portable Runtime (APR) iconv library"
HOMEPAGE = "http://apr.apache.org/"
SECTION = "libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b1ba065e9ab3b934451c83ec1c95d966"

SRC_URI = "${APACHE_MIRROR}/apr/${BPN}-${PV}.tar.gz"
SRC_URI[md5sum] = "60ae6f95ee4fdd413cf7472fd9c776e3"
SRC_URI[sha256sum] = "ce94c7722ede927ce1e5a368675ace17d96d60ff9b8918df216ee5c1298c6a5e"

ALLOW_EMPTY:${PN} = "1"
