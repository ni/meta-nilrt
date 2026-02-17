# PXIe-88XX series hardware requires the r600_dri driver for hardware rendering

# Upstream Mesa refactor removed PACKAGECONFIG[r600] and moved r600 under the
# AMD vendor group. Enabling PACKAGECONFIG[amd] now automatically adds r600 to
# the Gallium drivers.
PACKAGECONFIG:append = "\
   amd \
"

# mesa-native only inherits global DISTRO_FEATURES. Upstream Mesa now
# applies ANY_OF_DISTRO_FEATURES globally, so mesa-native is skipped
# unless opengl/opencl/vulkan is enabled globally.
DISTRO_FEATURES:append = " opengl"
