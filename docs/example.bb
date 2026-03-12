SUMMARY = "foobar - The example project"
DESCRIPTION = "\
foobar is an example project, used when you need to communicate concepts to \
developers."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "test"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"


# ==============================================================================
# RECIPE VARIABLES
# ==============================================================================

# If the recipe sources are entirely within OE, we can set the package version
# to match the DISTRO_VERSION.
# PV = "${DISTRO_VERSION}"


# ==============================================================================
# SOURCE VARIABLES
# ==============================================================================

SRC_URI = "\
	file://foo-file.1 \
	file://foo-file.2 \
	file://foo.initd \
"

S = "${UNPACKDIR}"


# ==============================================================================
# BBCLASSES
# ==============================================================================

# UPDATE-RC.D
inherit update-rc.d
INITSCRIPT_PARAMS = "default"
INITSCRIPT = "foo"


# ==============================================================================
# TASKS
# ==============================================================================

pkglibdir = "${libdir}/${BPN}"

do_install () {
	install -d ${D}${sysconfdir}/init.d
	install foo.initd ${D}${sysconfdir}/init.d/foo

	install -d ${D}${pkglibdir}
	install --mode=0755 foo-file.1 ${D}${pkglibdir}/foo-file.1
	install --mode=0744 foo-file.2 ${D}${pkglibdir}/foo-file.2
}


# ==============================================================================
# PACKAGING
# ==============================================================================
# FOO
RDEPENDS:${PN} = "bash"


# ==============================================================================
# CLASS EXTENSIONS
# ==============================================================================
# BBCLASSEXTEND = "native"
