FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
            file://0001-Revert-opkg-feed-Fix-adding-feeds-with-same-name-as-.patch \
            file://0002-parse-user_defined_fields-in-control-file.patch \
            "

