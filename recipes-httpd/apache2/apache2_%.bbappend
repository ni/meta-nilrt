ENABLE_SRC_INSTALL_${PN} = "1"
PACKAGES += " ${PN}-src "

PACKAGECONFIG_append = " openldap "
