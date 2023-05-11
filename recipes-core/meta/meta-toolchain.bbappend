# The installation script redirects symlinks that point to
# /usr/local/oe-sdk-hardcoded-buildpath, so avoid causing errors for
# outgoing symlinks that are there by design.
CHECK_SDK_SYSROOTS = "0"

# Add the package sources needed when building the SDK with BUILD_IMAGES_FROM_FEEDS
IPK_FEED_URIS:append:task-populate-sdk = "${@oe.utils.conditional('BUILD_IMAGES_FROM_FEEDS', '1', '\
    NIOE-dummy-sdk##${NILRT_LOCAL_FEED_URI}/sdk-provides-dummy-target \
    NIOE-nativesdk##${NILRT_LOCAL_FEED_URI}/${SDK_ARCH}-${SDKPKGSUFFIX} \
    NIOE-dummy-nativesdk##${NILRT_LOCAL_FEED_URI}/sdk-provides-dummy-${SDKPKGSUFFIX} \
', '', d)}"
