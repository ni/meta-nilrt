FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

PV = "2.02+git${SRCPV}"
SRCREV = "61a02ce279575ea846e6ee7f8c9fb686fd54328c"

SRC_URI = "\
	${NILRT_GIT}/grub.git;branch=${GRUB_BRANCH};protocol=https \
	file://grub.cfg \
	file://cfg \
	file://grub.d \
"

S = "${WORKDIR}/git"

# Grub always try to force soft float in recent grub versions, even on x64, and this
# conflicts with how the x64 OE toolchain is set up. The only solution is to cache
# the empty variable to empty instead of no or yes, which will fail the build.
# More details: http://git.savannah.gnu.org/cgit/grub.git/tree/configure.ac?id=ce95549cc54b5d6f494608a7c390dba3aab4fba7#n790
CACHED_CONFIGUREVARS += "grub_cv_target_cc_soft_float="
