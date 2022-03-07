# overwrite the standard OE start_getty with our ni-specific one; start_getty
# is already added in SRC_URI so we just put our path first so OE fetches it.
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

# The console specified here is used as default by start_getty in case a real console
# isn't detected from /proc/consoles (happens when console is disabled from MAX).
# If a console isn't detected, we don't want start_getty to exit as that would
# cause sysvinit to respawn it.
# Using tty2 here as it isn't used elsewhere.
SERIAL_CONSOLES = "38400;tty2"
