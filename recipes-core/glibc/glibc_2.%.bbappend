
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

SRC_URI =+ "file://cp936_support.patch \
            file://cp936-gconv-modules.patch \
           "

SRC_URI =+ "file://0001-pi-condvars-add-protocol-support-to-pthread_condattr.patch \
            file://0002-pi-condvars-add-tst-condpi1.c-basic-API-test.patch \
            file://0003-pi-condvars-add-tst-condpi2.c-priority-inversion-avo.patch \
            file://0004-benchtests-Add-benchmarks-for-pthread_cond_-function.patch \
            file://0005-x86_64-Remove-assembly-implementations-for-pthread_c.patch \
            file://0006-pi-condvars-EAGAIN-retry-in-pthread_cond_-broadcast-and-signal.patch \
           "
# Add patch to build the ja_JP.WINDOWS-31J locale for LabVIEW
# Japanese language support.
SRC_URI =+ " \
	file://windows-31j_support.patch \
"

# Add patch to alias custom LabVIEW locales to the equivalent
# glibc locales in the Base System Image
SRC_URI =+ " \
	file://alias-custom-locales.patch \
"
