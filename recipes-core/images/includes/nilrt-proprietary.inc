# NI infrastructure packages to be installed in the image

NI_PROPRIETARY_SAFEMODE_PACKAGES = "\
        ni-software-installation-daemon \
"

NI_PROPRIETARY_COMMON_PACKAGES = "\
        libnitargetcfg \
        ni-auth \
        ni-auth-webservice \
        ni-avahi-client \
        ni-ca-certs \
        ni-rt-exec-webservice \
        ni-service-locator \
        ni-software-action-services \
        ni-software-installation-websvc \
        ni-ssl-webserver-support \
        ni-sysapi \
        ni-sysapi-remote \
        ni-sysapi-webservice \
        ni-sysmgmt-auth-utils \
        ni-system-webserver \
        ni-traceengine \
        ni-webdav-system-webserver-support \
        ni-webserver-libs \
        ni-webservices-webserver-support \
        ni-wif-landingpage \
        nicurl \
        nirtcfg \
        nirtmdnsd \
"

# TODO: ni-wifibledd and ni-wireless-ath6kl are required for ARM but there are
# no ARM builds for them. Add them back to NI_PROPRIETARY_COMMON_PACKAGES when
# there are.
NI_PROPRIETARY_COMMON_PACKAGES:append:x64 = "\
        ni-sdmon \
        ni-wifibledd \
        ni-wireless-ath6kl \
"

NI_PROPRIETARY_RUNMODE_PACKAGES = "\
        ni-arch-gen \
        python3-ni-asset-discovery \
"

# SystemLink dependencies
NI_PROPRIETARY_RUNMODE_PACKAGES:append = "\
        ni-sysmgmt-salt-minion-support \
        ni-sysmgmt-sysapi-expert \
        python3-ni-systemlink-sdk \
"
