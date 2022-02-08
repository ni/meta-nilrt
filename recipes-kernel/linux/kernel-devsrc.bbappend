require kernel-devsrc-nilrt.inc

KERNEL_VERSION = "${@get_kernelversion_headers('${B}')}"

RDEPENDS_${PN}:remove = "python3"
