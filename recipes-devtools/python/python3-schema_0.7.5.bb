SUMMARY = "Schema validation just got Pythonic"
DESCRIPTION = "\
schema is a library for validating Python data structures, such as those \
obtained from config-files, forms, external services or command-line parsing, \
converted from JSON/YAML (or something else) to Python data-types.\
"
HOMEPAGE = "https://github.com/keleshev/schema"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE-MIT;md5=09b77fb74986791a3d4a0e746a37d88f"


SRC_URI[sha256sum] = "f06717112c61895cabc4707752b88716e8420a8819d71404501e114f91043197"


inherit pypi setuptools3

BBCLASSEXTEND = "native nativesdk"
