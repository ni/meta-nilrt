FILESEXTRAPATHS_prepend := "${THISDIR}:${THISDIR}/files:"

SRC_URI += " \
        file://CVE-2016-5419.patch \
        file://CVE-2016-5420.patch \
        file://NTLM-check-for-NULL-pointer-before-deferencing.patch \
        file://0001-Updated-Curl_wait_ms-for-VxWorks.patch \
        file://0002-Including-wrn-coreip-hostLib.h-in-lib-hostip4.c-for-.patch \
        file://0003-Including-selectLib.h-in-lib-select.c-for-VxWorks-bu.patch \
        file://0004-Added-curl_easy_getinfo_va-API-function.patch \
        file://0005-Added-curl_easy_setopt_va-API-function.patch \
        file://0006-Added-curl_formadd_va-API-function.patch \
        file://0007-Added-curl_multi_setopt_va-API-function.patch \
        file://0008-Added-curl_share_setopt_va-API-function.patch \
        file://0009-Add-nicurl-wrapper-functions.patch \
        file://0010-Add-mutex-locking-and-unlocking.patch \
        file://0011-Made-curl_global_init-thread-safe.patch \
        file://0012-Made-curl_global_cleanup-thread-safe.patch \
        file://0013-Made-curl_easy_init-thread-safe.patch \
        file://0014-Updated-verifyconnect-for-PharLAP.patch \
        file://0015-Excluding-HAVE_WINSOCK2_H-and-HAVE_WS2TCPIP_H-on-Pha.patch \
        file://0016-Excluding-HAVE_RAND_SCREEN-on-PharLAP.patch \
        file://0017-Add-NI-specific-changes-to-VxWorks-config-file.patch \
        file://0018-Modified-include-curl-curlbuild.h.dist-for-NI-s-VxWo.patch \
        file://0019-Don-t-use-wincrypt.h-on-PharLap.patch \
        file://0021-Fixup-lib1529-test.patch \
        file://0022-config-win32.h-Disable-Win32-crypto-API-on-Phar-Lap-.patch \
        file://0023-Decorate-API-functions-on-Windows-with-__cdecl.patch \
"
