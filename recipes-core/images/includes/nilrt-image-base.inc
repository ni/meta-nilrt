# This image is bootable to a console with package management support

# useradd and groupadd need to be on sysroot
do_rootfs[depends] += "shadow-native:do_populate_sysroot"

# kernel recipe requires depmodwrapper to populate modules.dep
do_rootfs[depends] += "depmodwrapper-cross:do_populate_sysroot"

# without package-management update-rc.d gets removed from image
IMAGE_FEATURES += "package-management"

IMAGE_INSTALL += "\
	packagegroup-ni-base \
	packagegroup-ni-tzdata \
"

addtask image_build_test before do_rootfs

# We always want package-management support in this image, fail if not enabled
do_image_build_test() {
	if ! echo ${IMAGE_FEATURES} | grep -q "package-management";then
		exit 1
	fi
}

# Guarantee a usable LOCAL root login.
#
# Historically the default interactive login was the NIAuth-backed 'admin' user
# (empty password), which was only visible/usable because NIAuth's NSS and PAM
# modules were pulled in transitively by ni-auth. As of dist-next, ni-auth no
# longer installs those modules, so the NIAuth 'admin'/"" login is gone. Make the
# local root account loginable out of the box (the box ships open and is expected
# to be secured on first configuration; see FEATURE 3835079):
#
#   * empty-root-password  - keep root's /etc/shadow password field empty
#                            (otherwise the rootfs postprocess zaps it to '*').
#   * allow-empty-password - rewrite 'pam_unix.so nullok_secure' to plain
#                            'nullok' in /etc/pam.d/* and set sshd
#                            PermitEmptyPasswords. nullok_secure only permits
#                            blank passwords from ttys listed in /etc/securetty,
#                            which excludes SSH (PAM_TTY=ssh), so without this an
#                            empty root password could not log in over SSH.
#
IMAGE_FEATURES += "empty-root-password allow-empty-password"

python install_additional_feeds() {
    for source_name, uri in d.getVarFlags('PACKAGE_FEED_URIS_ADDITIONAL').items():
        source_conf = os.path.join(d.getVar('IMAGE_ROOTFS'), 'etc', 'opkg', ('%s.conf' % source_name))
        bb.debug(1, 'Writing additional opkg sources to %s' % source_conf)
        with open(source_conf, 'w') as fp_conf:
            real_uri = d.getVarFlag('PACKAGE_FEED_URIS_ADDITIONAL', source_name, expand=True)
            if real_uri is None:
                bb.error('Source feed URI for source "%s" failed to expand.' % source_name)
            else:
                bb.debug(1, '%s -> %s' % (source_name, real_uri))
                fp_conf.write('src/gz %s %s\n' % (source_name, real_uri))
}

ROOTFS_POSTPROCESS_COMMAND += "\
    install_additional_feeds; \
"

require nilrt-core-image.inc
