FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
	file://0001-Added-curl_easy_getinfo_va-API-function.patch \
	file://0002-Added-curl_formadd_va-API-function.patch \
	file://0003-Added-curl_multi_setopt_va-API-function.patch \
	file://0004-Added-curl_share_setopt_va-API-function.patch \
	file://0005-Add-nicurl-wrapper-functions.patch \
	file://0006-Add-global-init-mutex.patch \
	file://0007-Made-curl_global_init-thread-safe.patch \
	file://0008-Made-curl_global_cleanup-thread-safe.patch \
	file://0009-Made-curl_easy_init-thread-safe.patch \
"

SELECTED_OPTIMIZATION += "-Wno-deprecated-declarations"

ENABLE_SRC_INSTALL_${PN} = "1"
