DESCRIPTION = "A small console image."


IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp"

IMAGE_DEV_MANAGER = "busybox-mdev"

IMAGE_INSTALL = "\
	packagegroup-core-boot \
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
	packagegroup-ni-runmode \
	"

IMAGE_FSTYPES = "tar.bz2 ext3 vmdk"

DEPENDS = "zip-native"

require include/niconsole-image.inc

#Set password to blank
ROOTFS_POSTPROCESS_COMMAND += "sed -i -e's/root:NP:/root::/' ${IMAGE_ROOTFS}/etc/shadow; "

inherit image

# make sure we have some free space (in Kbytes)
IMAGE_ROOTFS_EXTRA_SPACE = "102400"

SRC_URI = "file://machine.vmx"

# remove not needed ipkg informations
ROOTFS_POSTPROCESS_COMMAND += "remove_packaging_data_files ; "

SYSLINUX_ROOT = "root=/dev/hda2 rw "

addtask rootfs after do_unpack

python () {
	# Ensure we run these usually noexec tasks
	d.delVarFlag("do_fetch", "noexec")
	d.delVarFlag("do_unpack", "noexec")
}

create_bundle_files () {
	cd ${DEPLOY_DIR_IMAGE}
	mkdir -p vm-${IMAGE_NAME}
	cp -f ${WORKDIR}/machine.vmx vm-${IMAGE_NAME}/${IMAGE_NAME}.vmx
	chmod 0644 vm-${IMAGE_NAME}/${IMAGE_NAME}.vmx
	mv -f ${IMAGE_NAME}.vmdk vm-${IMAGE_NAME}/
	sed -i "s/machine_disk\.vmdk/${IMAGE_NAME}\.vmdk/g" vm-${IMAGE_NAME}/${IMAGE_NAME}.vmx
	zip -r vm-${IMAGE_NAME}.zip vm-${IMAGE_NAME}
	ln -sf vm-${IMAGE_NAME}.zip vm-${IMAGE_LINK_NAME}.zip
	rm -rf vm-${IMAGE_NAME}
	rm -f ${IMAGE_NAME}.hdddirect
	rm -f ${IMAGE_LINK_NAME}.hdddirect
	rm -f ${IMAGE_LINK_NAME}.vmdk
}

python do_bundle_files() {
    bb.build.exec_func('create_bundle_files', d)
}

addtask bundle_files after do_vmdkimg before do_build
do_bundle_files[nostamp] = "1"
