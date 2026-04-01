# This include provides common logic for making NILRT container
# images.

# The ROOTFS_RO subsystem triggers opkg remove() operations after
# the rootfs is made. It isn't intended to be used to deinstall
# these kinds of packages. But we're using it for that anyway.
FORCE_RO_REMOVE = "1"
# The packages in ROOTFS_RO_UNNEEDED MUST be exact package names.
# The backing func will test the installed package manifest and only
# issue an opkg remove if the package name is present. So no wildcards.
ROOTFS_RO_UNNEEDED = "\
	ni-arch-gen \
	kernel-devsrc \
"

# Necessary whenever building a container.
# Though this appears to do nothing, given how our kernel recipes are constructed.
# The kernel and modules are still installed to the rootfs.
PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"


#
# image type configuration block
#
OCI_IMAGE_AUTHOR ?= "Emerson"
OCI_IMAGE_AUTHOR_EMAIL ?= "rtos@emerson.com"

# IMAGE_TAG cannot contain the image name, when using sloci.
OCI_IMAGE_TAG = "${DISTRO_VERSION}"

OCI_IMAGE_ARCH ?= "${@oe.go.map_arch(d.getVar('TARGET_ARCH'))}"
OCI_IMAGE_SUBARCH ?= "${@oci_map_subarch(d.getVar('TARGET_ARCH'), d.getVar('TUNE_FEATURES'), d)}"

OCI_IMAGE_ENTRYPOINT = "/init"
OCI_IMAGE_ENTRYPOINT_ARGS = "/bin/bash"

# Cannot use the umoci OCI backend. umoci does not preserve non-root ownership in the rootfs, breaking many files that are owned by root:ni or lvuser:ni.
OCI_IMAGE_BACKEND = "sloci-image"


inherit image-oci
inherit image-oci-docker

IMAGE_FSTYPES = "container oci docker"


SFILES = "${THISDIR}/files/container"

fakeroot container_rootfs_post() {
	echo "VARIANT_ID=container" >>${IMAGE_ROOTFS}/etc/os-release

	mkdir -p ${IMAGE_ROOTFS}/boot/grub
	install ${SFILES}/grubenv ${IMAGE_ROOTFS}/boot/grub/grubenv

	install -m 544 ${SFILES}/init ${IMAGE_ROOTFS}/init
}
ROOTFS_POSTPROCESS_COMMAND += " container_rootfs_post; "


fakeroot container_image_pre() {
	# configure opkg
	echo "arch DeviceCode77E1 51" >>${IMAGE_ROOTFS}/${sysconfdir}/opkg/arch.conf

	# remake some removed volatile directories that do_rootfs removes
	install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
	install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log

	# Workaround for nirtcfg's ni-rt.ini.lock bug
	# /run/lock cannot have the sticky-bit set.
	install -m 777 -d ${IMAGE_ROOTFS}/run/lock
}
IMAGE_PREPROCESS_COMMAND += " container_image_pre; "
