FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
        file://0004-Added-curl_easy_getinfo_va-API-function.patch \
        file://0006-Added-curl_formadd_va-API-function.patch \
        file://0007-Added-curl_multi_setopt_va-API-function.patch \
        file://0008-Added-curl_share_setopt_va-API-function.patch \
        file://0009-Add-nicurl-wrapper-functions.patch \
        file://0010-Add-mutex-locking-and-unlocking.patch \
        file://0011-Made-curl_global_init-thread-safe.patch \
        file://0012-Made-curl_global_cleanup-thread-safe.patch \
        file://0013-Made-curl_easy_init-thread-safe.patch \
        file://0021-Fixup-lib1529-test.patch \
        file://0023-Decorate-API-functions-on-Windows-with-__cdecl.patch \
        file://0024-curl-system.h-define-SIZEOF_CURL_OFF_T.patch \
        file://0025-global_init-restore-CURL_GLOBAL_SSL-option.patch \
"

SELECTED_OPTIMIZATION += "-Wno-deprecated-declarations"

ENABLE_SRC_INSTALL_${PN} = "1"
PACKAGE_DEBUG_SPLIT_STYLE = "debug-with-srcpkg"
