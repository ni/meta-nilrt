require kernel-devsrc-nilrt.inc

# "make prepare" skips building objtool if elfutils-dev is not installed at
# postinst time; however if you install elfutils-dev later, then when dkms
# tries to build modules it expects objtool to be available, but it's not.
# Depending on elfutils-dev forces it to be there during postinst.
RDEPENDS_${PN}_append += "${@bb.utils.contains('ARCH', 'x86', 'elfutils-dev', '', d)}"
