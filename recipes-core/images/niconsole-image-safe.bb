DESCRIPTION = "Safemode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL = "\
	packagegroup-ni-base \
	packagegroup-ni-safemode \
	packagegroup-ni-tzdata \
	packagegroup-ni-wifi \
	"

BAD_RECOMMENDATIONS += "shared-mime-info"

ROOTFS_POSTPROCESS_COMMAND += "\
	remove_unused_safemode_pkgs; \
"

remove_unused_safemode_pkgs() {
	opkg -o ${IMAGE_ROOTFS} -f ${IPKGCONF_TARGET} --force-depends remove linux-firmware-radeon
}

require niconsole-image.inc
