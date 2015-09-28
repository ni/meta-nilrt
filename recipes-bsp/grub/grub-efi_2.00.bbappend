# This .bbappend file changes the origin of source files and
# uses git instead of ftp.

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRCREV = "9585647a25b65f98db6bd22c569b34795512f046"
# Uses the same commit and the same patches which are used by grub in recoverytool
# (/nilinux/bootloader/grub2)

SRC_URI = "git://git.savannah.gnu.org/grub.git \
           file://grub-2.00-fpmath-sse-387-fix.patch \
           file://autogen.sh-exclude-pc.patch \
           file://0001-Fix-build-with-glibc-2.20.patch \
           file://grub-2.00-add-oe-kernel.patch \
           file://cfg \
           file://add_inbit_comm.patch \
	   file://grub-efi-cleanup-menu.patch \
           "

S = "${WORKDIR}/git"

do_configure_prepend() {
    ( cd ${S}
      ${S}/autogen.sh )
}
