FILESEXTRAPATHS_prepend := "${THISDIR}/grub:"

PV = "2.02+git${SRCPV}"
SRCREV = "832e7862089a7cbc985ae09a17f6827de6c85bf8"

SRC_URI = "\
	${NILRT_GIT}/grub.git;branch=${GRUB_BRANCH} \
	file://x86-64-Treat-R_X86_64_PLT32-as-R_X86_64_PC32.patch \
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
