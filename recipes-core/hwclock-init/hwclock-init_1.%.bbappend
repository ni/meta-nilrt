# Do not perform update-rc.d actions on the hwclock.sh initscript in this
# package. We only wish to call hwclock.sh from /etc/init.d/bootmisc manually.
INITSCRIPT_PACKAGES_remove = "${PN}"
