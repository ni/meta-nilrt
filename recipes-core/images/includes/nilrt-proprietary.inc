# NI infrastructure packages to be installed in the image

NI_PROPRIETARY_SAFEMODE_PACKAGES = "\
        ni-software-installation-daemon \
"

NI_PROPRIETARY_COMMON_PACKAGES = "\
        libnitargetcfg \
        ni-avahi-client \
        ni-ca-certs \
        ni-service-locator \
        ni-software-action-services \
        ni-sysmgmt-auth-utils \
        ni-traceengine \
        ni-webserver-libs \
        ni-wifibledd \
        nicurl \
        nirtcfg \
        nirtmdnsd \
"

NI_PROPRIETARY_COMMON_PACKAGES:append:x64 = "\
        ni-sdmon \
        ni-sysapi-sshcli \
"

NI_PROPRIETARY_RUNMODE_PACKAGES = "\
        ni-arch-gen \
"

# SystemLink dependencies
NI_PROPRIETARY_RUNMODE_PACKAGES:append = "\
        ni-sysmgmt-salt-minion-support \
        ni-sysmgmt-sysapi-expert \
        python3-ni-asset-discovery \
        python3-ni-systemlink-sdk \
"
