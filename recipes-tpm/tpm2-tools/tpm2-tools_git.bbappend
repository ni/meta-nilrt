DEFAULT_PREFERENCE = "1"

SRC_URI = "${NILRT_GIT}/tpm2-tools.git;branch=nilrt/18.1"

S = "${WORKDIR}/git"

# disable the resource daemon dependency (tpm2-abrmd) so it won't get pulled
# into images via pkggroup-base -> nilrtdiskcrypt -> tpm2-tools -> tpm2-abrmd
# because it breaks nilrtdiskcrypt_quote functionality
EXTRA_OECONF += "--without-tcti-tabrmd"

# These GCC 7.3 generated warnings are fixed in newer tpm2-tools branches
# so we should drop this -Wno-error once we rebase the tpm2-* repos
CFLAGS += "-Wno-error"
