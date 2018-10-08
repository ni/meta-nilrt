# disable image features we don't need
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
FEED_URIS = ""

IMAGE_FSTYPES_append += "squashfs"

BAD_RECOMMENDATIONS += "shared-mime-info gawk coreutils bison"

# clean-up files we don't need
do_cleanup() {
	rm -rf ${IMAGE_ROOTFS}/{home,boot,var,sbin,dev,etc,media,mnt,sys,proc,tmp}
}

IMAGE_PREPROCESS_COMMAND += "do_cleanup; "

inherit image
