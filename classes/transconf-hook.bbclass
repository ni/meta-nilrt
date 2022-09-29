# This class adds a '-transconf' subpackage, for installing trasnconf hooks
# accociated with a package.
#
# To use this class:
# * ``inherit transconf-hook``
# * Define your hook scripts using the ``TRANSCONF_HOOKS_${PN}`` variable.
#   * These hooks will automatically be installed to the
#   ${TRANSCONF_HOOKS_RUNPARTS_PATH} location and added to the subpackage's
#   files.
SUMMARY:${PN}-transconf ?= "${SUMMARY} - Transconf hooks"
DESCRIPTION:${PN}-transconf ?= "${DESCRIPTION} \
This package contains transconf hooks, used to migrate the parent package's \
configuration files between images."

PACKAGES =+ "${PN}-transconf"
RDEPENDS:${PN}-transconf += "${PN} transconf"
SECTION:${PN}-transconf = "Infrastructure"

TRANSCONF_HOOKS_RUNPARTS_PATH ?= "${sysconfdir}/transconf/hooks"
TRANSCONF_HOOKS_${PN} ?= ""

FILES:${PN}-transconf = "${TRANSCONF_HOOKS_RUNPARTS_PATH}"

do_install:append() {
	install -d "${D}${TRANSCONF_HOOKS_RUNPARTS_PATH}"

	for hook in "${TRANSCONF_HOOKS_${PN}}"; do
		install -m 0755 "${WORKDIR}/${hook}" "${D}${TRANSCONF_HOOKS_RUNPARTS_PATH}"
	done
}
