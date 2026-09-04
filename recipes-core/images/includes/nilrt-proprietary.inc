# NI infrastructure packages to be installed in the image

NI_PROPRIETARY_SAFEMODE_PACKAGES = "\
        ni-software-installation-daemon \
"

NI_PROPRIETARY_COMMON_PACKAGES = "\
        libnitargetcfg \
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
        ni-sysmgmt-auth-utils \
        ni-system-webserver \
        ni-traceengine \
        ni-webdav-system-webserver-support \
        ni-webserver-libs \
        ni-webservices-webserver-support \
        ni-wif-landingpage \
        ni-wifibledd \
        nicurl \
        nirtcfg \
        nirtmdnsd \
"

NI_PROPRIETARY_COMMON_PACKAGES:append:x64 = "\
        ni-sdmon \
        ni-wifibledd \
        ni-sysapi-sshcli \
"

NI_PROPRIETARY_RUNMODE_PACKAGES = "\
        ni-arch-gen \
        ni-niauth-root-user \
"

# SystemLink dependencies
NI_PROPRIETARY_RUNMODE_PACKAGES:append = "\
        ni-sysmgmt-salt-minion-support \
        ni-sysmgmt-sysapi-expert \
        python3-ni-asset-discovery \
        python3-ni-systemlink-sdk \
"
