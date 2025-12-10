# PXIe-88XX series hardware requires the r600_dri driver for hardware rendering

# Upstream Mesa refactor removed PACKAGECONFIG[r600] and moved r600 under the
# AMD vendor group. Enabling PACKAGECONFIG[amd] now automatically adds r600 to
# the Gallium drivers.
PACKAGECONFIG:append = "\
   amd \
"
