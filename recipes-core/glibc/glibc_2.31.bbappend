FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/${PN}:"

# Add patches to map CP936 to the proper codepage for LabVIEW simplified
# Chinese support.
SRC_URI =+ " \
	file://cp936_support.patch \
	file://cp936-gconv-modules.patch \
"
