# overwrite the standard OE start_getty with our ni-specific one; start_getty
# is already added in SRC_URI so we just put our path first so OE fetches it.
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
