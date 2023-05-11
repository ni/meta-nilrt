# Normally no package index is created for the following DUMMYARCH but
# we need one for building SDKs and meta-toolchain from feeds.
PACKAGE_ARCHS:append = " sdk-provides-dummy-target"
SDK_PACKAGE_ARCHS:append = " sdk-provides-dummy-${SDKPKGSUFFIX}"
