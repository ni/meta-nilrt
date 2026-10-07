FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI:append:class-target = " file://0001-xz-restore-single-threaded-default.patch"
