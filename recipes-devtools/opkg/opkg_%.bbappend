FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
            file://0001-libopkg-Add-intercepts_dir-option-to-opkg.conf.patch \
            file://0001-pkg_get_provider_replacees-do-not-add-installed-pkg-.patch \
            file://opkg.conf \
           "
