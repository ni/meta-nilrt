# The OE-core setuptools3-base bbclass gives gpgme an RDEPENDS for
# python3-core, even though the gpgme python components are segregated to their
# own subpackages.
RDEPENDS:${PN}:remove = "${PYTHON_PN}-core"
