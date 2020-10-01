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
	file://0010-Updated-verifyconnect-for-PharLAP.patch \
	file://0011-Excluding-HAVE_WINSOCK2_H-and-HAVE_WS2TCPIP_H-on-Pha.patch \
	file://0012-Excluding-HAVE_RAND_SCREEN-on-PharLAP.patch \
	file://0013-Don-t-use-wincrypt.h-on-PharLap.patch \
	file://0014-Fixup-lib1529-test.patch \
	file://0015-config-win32.h-Disable-Win32-crypto-API-on-Phar-Lap-.patch \
	file://0016-Decorate-API-functions-on-Windows-with-__cdecl.patch \
	file://0017-curl-system.h-define-CURL_SIZEOF_CURL_OFF_T.patch \
	file://0018-Revert-global_init-ignore-CURL_GLOBAL_SSL-s-absense.patch \
	file://0019-win32-disable-USE_UNIX_SOCKETS-entirely.patch \
	file://0020-transfer.c-avoid-WSAIoctl-on-PHARLAP.patch \
	file://0021-chkdecimalpoint-use-default-libs.patch \
"

SELECTED_OPTIMIZATION += "-Wno-deprecated-declarations"

ENABLE_SRC_INSTALL_${PN} = "1"
