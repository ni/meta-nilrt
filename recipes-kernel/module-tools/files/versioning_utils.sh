ULNTools=/usr/local/natinst/tools

TOOLS_SQFS=tools-squashfs-image.squashfs
TOOLS_MNTPNT=/var/volatile/tmp/tools

BINUTILS_FILES=( addr2line ar as c++filt elfedit gprof ld ld.bfd nm objcopy objdump ranlib readelf size strings strip )

# Private-ish functions, don't call these unless you know what you are doing
function mount_proc
{
	[ -f /proc/cmdline ] || mount -t proc none /proc
}

function mount_sysfs
{
	[ -d /sys/bus ] || mount -t sysfs none /sys
}

function squashfs_mount
{
	[ -e "$1" ] || return
	[ -d "$2" ] || return
	local sfs=${1}
	local mp=${2}
	mount -t squashfs ${sfs} ${mp}
}

function squashfs_umount
{
	[ -d "$1" ] || return
	local mp=${1}
	umount ${mp}
}

function create_loopdevs
{
	[ -b /dev/loop0 ] || { for dn in `seq 0 7`; do mknod -m660 /dev/loop$dn b 7 $dn; done }
}

function mount_tools
{
	mount_proc
	[ -z "`mount | grep ${TOOLS_SQFS}`" ] || return 0
	mkdir -p ${TOOLS_MNTPNT}
	create_loopdevs
	squashfs_mount ${ULNTools}/${TOOLS_SQFS} ${TOOLS_MNTPNT}
}

function umount_tools
{
	[ -z "`mount | grep ${TOOLS_SQFS}`" ] && return 0
	squashfs_umount ${TOOLS_MNTPNT}
}

function fix_dev_fd
{
	mount_proc || return
	[ -d /dev/fd ] || ln -s /proc/self/fd /dev/fd
	[ -e /dev/stderr ] || ln -s /dev/fd/2 /dev/stderr
	[ -e /dev/stdout ] || ln -s /dev/fd/1 /dev/stdout
}

function fixup_binutils_symlinks
{
	gcc_name=$(readlink ${TOOLS_MNTPNT}/usr/bin/gcc)
	gcc_name=${gcc_name##*/}
	binutils_prefix=${gcc_name%%-gcc}

	for binutils_file in "${BINUTILS_FILES[@]}"
	do
		update-alternatives --install /usr/bin/${binutils_file} ${binutils_file} ${TOOLS_MNTPNT}/usr/bin/${binutils_prefix}-${binutils_file} 100
	done
}

function remove_binutils_symlinks
{
	gcc_name=$(readlink ${TOOLS_MNTPNT}/usr/bin/gcc)
	gcc_name=${gcc_name##*/}
	binutils_prefix=${gcc_name%%-gcc}

	for binutils_file in "${BINUTILS_FILES[@]}"
	do
		update-alternatives --remove ${binutils_file} ${TOOLS_MNTPNT}/usr/bin/${binutils_prefix}-${binutils_file}
	done
}

function setup_versioning_env
{
	mount_tools && \
	mount_sysfs && \
	fix_dev_fd && \
	fixup_binutils_symlinks
}

function cleanup_versioning_env
{
	depmod $(kernel_version) && \
	umount_tools && \
	remove_binutils_symlinks
}

function versioning_call
{
	LD_LIBRARY_PATH=${TOOLS_MNTPNT}/lib:${TOOLS_MNTPNT}/usr/lib PATH=${PATH}:${TOOLS_MNTPNT}/usr/bin NO_INSTALL_UTIL=1 KERNELHEADERS=${TOOLS_MNTPNT}/kernel CFLAGS=--sysroot=${TOOLS_MNTPNT} "$@"
}

function kernel_version
{
	mount_tools || return
	[ -e ${ULNTools}/kernel_version ] || ( find ${TOOLS_MNTPNT}/kernel/include -name "utsrelease.h" -exec sed 's/^.*"\(.*\)".*$/\1/' {} \; > ${ULNTools}/kernel_version )
	cat ${ULNTools}/kernel_version
}

#Public functions - These are what should be used by postinst/prerm scripts

# install_nidriver
#
# This call is meant to be called from a driver's postinst script once the
# unversioned binary has been installed on the target for during-install
# versioning.
#
# Arguments -   $1 module name
#               $2 full-path to the unversioned kernel module
#
# Return -      An error condition if either the module name is omitted, the
#               unversioned kernel module does not exist, or if there is an
#               error when running the kal scripts.
#
# Example:  install_nidriver niriozynqk /usr/local/natinst/nirio/niriozynqk-unversioned.o

function install_nidriver
{
	set -e
	[ -n "$1" ] || return
	[ -e "$2" ] || return
	name=${1}
	uvmod=${2}

	setup_versioning_env

	versioning_call /usr/local/natinst/nikal/bin/nikalKernelInstaller.sh --linuxrt -i -d ${name} ${uvmod}

	cleanup_versioning_env
}

# uninstall_nidriver
#
# This function will unregister the driver with KAL, which will also remove
# generated kernel module files. This function should be called from your
# module's prerm.
#
# Arguments:    $1 - module name
#               $2 - path to unversioned object file
#
# Example: uninstall_nidriver moduletemplate /path/to/moduletemplate/src/objects/moduletemplate-unversioned.o

function uninstall_nidriver
{
	[ -n "$1" ] || return
	[ -e "$2" ] || return
	setup_versioning_env
	versioning_call /usr/local/natinst/nikal/bin/nikalKernelInstaller.sh --linuxrt -r -d $1 $2
	cleanup_versioning_env
}

