ni_external_components = "\
        nirtcfg \
        niminionagent \
"

ni_external_components_append_x86_64 = "\
        nisdmon \
"

IMAGE_INSTALL_NODEPS = "${ni_external_components}"
