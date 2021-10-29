FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

SRC_URI += "\
	file://cmd-test-Add-bitwise-AND-document-the-feature.patch \
	file://grub-advertise-NI-NILRT-over-GNU-GRUB.patch \
	file://add-inbit-command-to-io-module.patch \
	file://grub.cfg \
	file://cfg \
	file://grub.d \
"

# Grub always try to force soft float in recent grub versions, even on x64, and this
# conflicts with how the x64 OE toolchain is set up. The only solution is to cache
# the empty variable to empty instead of no or yes, which will fail the build.
# More details: http://git.savannah.gnu.org/cgit/grub.git/tree/configure.ac?id=ce95549cc54b5d6f494608a7c390dba3aab4fba7#n790
CACHED_CONFIGUREVARS += "grub_cv_target_cc_soft_float="
