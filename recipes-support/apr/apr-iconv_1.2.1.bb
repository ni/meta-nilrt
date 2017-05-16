SUMMARY = "Apache Portable Runtime (APR) iconv library"
HOMEPAGE = "http://apr.apache.org/"
SECTION = "libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b1ba065e9ab3b934451c83ec1c95d966"

SRC_URI = "${APACHE_MIRROR}/apr/${BPN}-${PV}.tar.gz"
SRC_URI[md5sum] = "4a27a1480e6862543396e59c4ffcdeb4"
SRC_URI[sha256sum] = "19381959d50c4a5f3b9c84d594a5f9ffb3809786919b3058281f4c87e1f4b245"

ALLOW_EMPTY_${PN} = "1"
