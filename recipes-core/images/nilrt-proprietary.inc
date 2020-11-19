# Disable running ldconfig at the end of the rootfs creation process.
# Running ldconfig removes symlinks created by ni packages installed
# to the rootfs, particularly the symlinks to libraries in
# NIWebServer. This needs to be investigated before ldconfig can be
# reenabled.
LDCONFIGDEPEND = ""

# NI infrastructure packages to be installed in the image
IMAGE_INSTALL_NODEPS = "\
        libnitargetcfg \
        ni-arch-gen \
        ni-auth \
        ni-auth-webservice \
        ni-avahi-client \
        ni-ca-certs \
        ni-rt-exec-webservice \
        ni-service-locator \
        ni-skyline-file-client \
        ni-skyline-message-client \
        ni-skyline-tag-client \
        ni-software-action-services \
        ni-software-installation-websvc \
        ni-ssl-webserver-support \
        ni-sysapi \
        ni-sysapi-remote \
        ni-sysapi-webservice \
        ni-sysmgmt-auth-utils \
        ni-sysmgmt-salt-minion-support \
        ni-sysmgmt-sysapi-expert \
        ni-system-webserver \
        ni-traceengine \
        ni-webdav-system-webserver-support \
        ni-webserver-libs \
        ni-webservices-webserver-support \
        nicurl \
        nirtcfg \
        nirtmdnsd \
        nissl \
        python3-ni-systemlink-sdk \
        sysconfig-settings \
        sysconfig-settings-ui \
"
