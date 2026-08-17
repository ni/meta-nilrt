IMAGE_FEATURES[validitems] += "allow-setting-blank-passwords"

enable_setting_blank_passwords() {
	sed -i -E '/^password[[:space:]]+\[success=1 default=ignore\][[:space:]]+pam_unix\.so([[:space:]]|$)/ {
		/[[:space:]]nullok([[:space:]]|$)/! s/(pam_unix\.so)([[:space:]]|$)/\1 nullok\2/
	}' "${IMAGE_ROOTFS}${sysconfdir}/pam.d/common-password"
}

ROOTFS_POSTPROCESS_COMMAND += '${@bb.utils.contains("IMAGE_FEATURES", "allow-setting-blank-passwords", "enable_setting_blank_passwords; ", "", d)}'
