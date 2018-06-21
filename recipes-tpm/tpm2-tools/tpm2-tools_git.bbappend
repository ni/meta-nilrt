DEFAULT_PREFERENCE = "1"

SRC_URI = "${NILRT_GIT}/tpm2-tools.git;branch=nilrt/18.0"

S = "${WORKDIR}/git"

# These GCC 7.3 generated warnings are fixed in newer tpm2-tools branches
# so we should drop this -Wno-error once we rebase the tpm2-* repos
CFLAGS += "-Wno-error"
