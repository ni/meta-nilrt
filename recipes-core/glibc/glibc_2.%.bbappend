
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

## package: ni-locale-alias ##
PACKAGES =+ " ni-locale-alias "
FILES_ni-locale-alias = "${datadir}/locale/locale.alias"
SUMMARY_ni-locale-alias = "NI Locale Aliases"
DESCRIPTION_ni-locale-alias = "Custom locale.alias file for NI Software."

# Disable building split locale packages since those are empty.
# Locale files for glibc are built by glibc-locale.
PACKAGE_NO_LOCALE = "1"

# The stash_locale_package_cleanup function deletes the /usr/share/locale
# directory from the package/ staging area before it can be allocated to
# ni-locale-alias. Restore it before splitting subpackages.
restore_locale_alias() {
	install -D ${LOCALESTASH}${datadir}/locale/locale.alias ${PKGD}${datadir}/locale/locale.alias
}
PACKAGE_PREPROCESS_FUNCS += " restore_locale_alias "
