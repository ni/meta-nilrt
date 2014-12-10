PACKAGECONFIG = "eigen jpeg png tiff v4l \
                 ${@bb.utils.contains("DISTRO_FEATURES", "x11", "gtk", "", d)}"
