# This includes file contains content which is only appropriate for *alternate*
# (non-standard) versions of the kernl-devsrc package (eg. kernel-devsrc-next).

do_install[depends] += "${KERNEL_DEPENDENCY}:do_shared_workdir"
do_install[depends] += "${KERNEL_DEPENDENCY}:do_install"
do_configure[depends] += "${KERNEL_DEPENDENCY}:do_deploy"

S = "${KERNEL_DIRECTORY}"
B = "${KERNEL_BUILD_DIRECTORY}"

KERNEL_VERSION = "${@get_kernelversion_headers('${B}')}"

do_install:append() {
	# Remove the symlink so it doesn't conflict with other
	# kernel-devsrc packages
	rm -f ${D}/usr/src/kernel
	rmdir --ignore-fail-on-non-empty ${D}/usr/src
}

RDEPENDS:${PN}:remove = "python3"
