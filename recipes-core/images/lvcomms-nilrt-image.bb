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
    nitaglv \
    nisvcloc \
    ni-nwstreams \
    ni-flexrio \
    ni-usrp-rio \
    mt \
"

IMAGE_INSTALL_NODEPS_remove_armv7a = " \
    ni-flexrio \
    ni-usrp-rio \
    mt \
"

IMAGE_INSTALL_NODEPS_append_x64 = " \
    ni-atca \
    ni-pxiplatformservices \
    ni-sync \
    ni-p2p \
    ni-watchdog \
"
