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
	kernel-devsrc \
"

# Ensure ni-arch-gen is available in all container variants to generate
# /etc/opkg/ni-arch.conf at first boot via run-postinsts.
IMAGE_INSTALL_NODEPS += "ni-arch-gen"

# ni-auth is required by SystemWebServer (segfaults without niauth_daemon).
# ni-system-webserver provides the web server for deployment and discovery
# by NI MAX and VeriStand.
# ni-webdav-system-webserver-support provides mod_nidav.so for WebDAV file
# deployment (how VeriStand deploys .rtexe to /c).
IMAGE_INSTALL_NODEPS += "ni-auth ni-system-webserver ni-webdav-system-webserver-support"

# nirtmdnsd provides NI RT mDNS service for target discovery.
IMAGE_INSTALL_NODEPS += "nirtmdnsd"

# ni-sysapi-sshcli is required by both container variants; install it
# explicitly since NODEPS skips dependency resolution.
IMAGE_INSTALL_NODEPS += "ni-sysapi-sshcli"

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
	install -m 0755 ${SFILES}/nilrt-container.postinst ${IMAGE_ROOTFS}/postinst

	# Shadow /usr/bin/opkg with a wrapper that restarts lvrt after
	# install/upgrade so newly-installed RT applications (e.g. the VeriStand
	# Engine) launch without requiring a container reboot. /usr/local/bin
	# precedes /usr/bin on PATH.
	install -d ${IMAGE_ROOTFS}${prefix}/local/bin
	install -m 0755 ${SFILES}/opkg-wrapper.sh ${IMAGE_ROOTFS}${prefix}/local/bin/opkg
}
ROOTFS_POSTPROCESS_COMMAND += " container_rootfs_post; "

# Inject dummy opkg status entries for packages that segfault in containers.
# This makes opkg believe ni-dim/ni-mdbg are already installed at a very high
# version, satisfying any ">=" constraints from dependent packages (e.g.
# ni-daqmx-ef requires ni-dim >= 26.3.0) without actually installing them.
CONTAINER_DUMMY_OPKG_PACKAGES = "ni-dim ni-dim-dkms ni-dim-libs ni-dim-sysapi libnidimu1 ni-mdbg ni-mdbg-dkms ni-mdbg-errors"
fakeroot container_inject_dummy_deps() {
	statusfile="${IMAGE_ROOTFS}/var/lib/opkg/status"
	infodir="${IMAGE_ROOTFS}/var/lib/opkg/info"
	install -d "$infodir"
	for pkg in ${CONTAINER_DUMMY_OPKG_PACKAGES}; do
		cat >> "$statusfile" <<EOF

Package: ${pkg}
Version: 99.0.0
Status: install ok installed
Architecture: all
Description: Dummy entry - prevents installation in containers
EOF
		# opkg expects an installed package to have a corresponding .list file.
		: > "$infodir/${pkg}.list"
	done
}
IMAGE_PREPROCESS_COMMAND += " container_inject_dummy_deps; "

fakeroot container_image_pre() {
	# remake some removed volatile directories that do_rootfs removes
	install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
	install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log

	# Workaround for nirtcfg's ni-rt.ini.lock bug
	# /run/lock cannot have the sticky-bit set.
	install -m 777 -d ${IMAGE_ROOTFS}/run/lock

	# Enable sshd by default in containers
	if ! grep -q '^\[systemsettings\]' ${IMAGE_ROOTFS}${sysconfdir}/natinst/share/ni-rt.ini 2>/dev/null; then
		printf '\n[systemsettings]\n' >> ${IMAGE_ROOTFS}${sysconfdir}/natinst/share/ni-rt.ini
	fi
	if grep -q '^sshd\.enabled=' ${IMAGE_ROOTFS}${sysconfdir}/natinst/share/ni-rt.ini 2>/dev/null; then
		sed -i 's/^sshd\.enabled=.*/sshd.enabled="True"/' ${IMAGE_ROOTFS}${sysconfdir}/natinst/share/ni-rt.ini
	else
		sed -i '/^\[systemsettings\]/a sshd.enabled="True"' ${IMAGE_ROOTFS}${sysconfdir}/natinst/share/ni-rt.ini
	fi
}
IMAGE_PREPROCESS_COMMAND += " container_image_pre; "
