# keyutils is required to be able to mount cifs shares when
# CONFIG_CIFS_DFS_UPCALL is enabled. So adding it as dependency for
# cifs-tools as cifs-tools is already a requirement to mount cifs shares.
RDEPENDS:${PN} += "keyutils"
