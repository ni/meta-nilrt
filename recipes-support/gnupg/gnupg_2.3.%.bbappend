# libksba has already been upgraded to version 1.6.3 in openembedded-core.
# However, this CVE is still getting flagged as other configurations call
# out gnupg 2.3.0 (inclusive) - 2.4.0 (exclusive). Upstream has already
# bumped gnupg in master so let's just ignore the CVE here.
CVE_CHECK_IGNORE:append = " CVE-2022-3515"
