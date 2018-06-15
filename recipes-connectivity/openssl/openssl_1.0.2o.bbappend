FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI_remove = "file://debian/pic.patch"

# Patches for NI VxWorks in NIBuild
SRC_URI += " \
	file://0001-crypto-ts-Implement-def_time_cb-on-VxWorks.patch \
	file://0002-ssl-Implement-get_current_time-on-VxWorks.patch \
	file://0003-crypto-bio-Implement-get_current_time-on-VxWorks.patch \
"

# Patches for NI Phar Lap ETS in NIBuild
SRC_URI += " \
	file://0004-crypto-Implement-OpenSSLDie-as-abort-on-Phar-Lap-ETS.patch \
	file://0005-crypto-Remove-unsupported-headers-in-cryptlib.c-on-P.patch \
	file://0006-crypto-rand-Disable-unsupported-readscreen-on-Phar-L.patch \
	file://0007-crypto-rand-Don-t-include-unsupported-Windows-header.patch \
	file://0008-rand_win.c-Stub-RAND_poll-on-Phar-Lap-ETS.patch \
	file://0009-crypto-des-Disable-WIN_CONSOLE_BUG-hack-on-Phar-Lap-.patch \
	file://0010-crypto-ui-Disable-WIN_CONSOLE_BUG-hack-on-Phar-Lap-E.patch \
	file://0011-crypto-bio-Disable-xopenlog-xcloselog-on-Phar-Lap-ET.patch \
"

# Patches for Windows in NIBuild
SRC_URI += " \
	file://0012-crypto-Use-i686-instead-of-i486-in-MASM-script.patch \
	file://0013-perlasm-x86masm.pl-make-it-work.patch \
"

ENABLE_SRC_INSTALL_${PN} = "1"
