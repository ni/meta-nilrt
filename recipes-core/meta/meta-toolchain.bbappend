# The installation script redirects symlinks that point to
# /usr/local/oe-sdk-hardcoded-buildpath, so avoid causing errors for
# outgoing symlinks that are there by design.
CHECK_SDK_SYSROOTS = "0"
