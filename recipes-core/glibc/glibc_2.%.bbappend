FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

# Add patches to map CP936 to the proper codepage for LabVIEW simplified
# Chinese support.
SRC_URI =+ " \
	file://cp936_support.patch \
	file://cp936-gconv-modules.patch \
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
