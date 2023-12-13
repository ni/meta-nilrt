FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
	file://0001-Added-curl_easy_getinfo_va-API-function.patch \
	file://0002-Added-curl_formadd_va-API-function.patch \
	file://0003-Added-curl_multi_setopt_va-API-function.patch \
	file://0004-Added-curl_share_setopt_va-API-function.patch \
	file://0005-Add-nicurl-wrapper-functions.patch \
"

SELECTED_OPTIMIZATION += "-Wno-deprecated-declarations"
