
FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

SRC_URI =+ "file://cp936_support.patch "

SRC_URI =+ "file://0001-pi-condvars-add-protocol-support-to-pthread_condattr.patch \
            file://0002-pi-condvars-add-tst-condpi1.c-basic-API-test.patch \
            file://0003-pi-condvars-add-tst-condpi2.c-priority-inversion-avo.patch \
            file://0004-benchtests-Add-benchmarks-for-pthread_cond_-function.patch \
            file://0005-x86_64-Remove-assembly-implementations-for-pthread_c.patch \
            file://0001-CVE-2015-7547-getaddrinfo-stack-based-buffer-overflo.patch \
           "
