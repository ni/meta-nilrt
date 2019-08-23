# Start with the minial image as a base
require minimal-nilrt-image.bb

# Change description
DESCRIPTION = " \
    Fully-featured run-mode image for LabVIEW Comms. Includes drivers, \
    daemons, and other utilities required for RT targets to interact \
    with Comms. \
"

# Append additional software
IMAGE_INSTALL_NODEPS += " \
    lvrt \
    ni-datacomms-lvinterface \
    libninwstreams \
    ni-service-locator \
    ni-varclient-rt \
"

IMAGE_INSTALL_NODEPS_append_x64 = " \
    mt \
    ni-flexrio \
    ni-p2p \
    ni-pxiplatformservices \
    ni-sync \
    niusrprio \
    ni-watchdog \
"
