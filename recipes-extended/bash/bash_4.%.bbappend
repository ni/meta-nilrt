# TEMPORARY WORKAROUND: Just like the majority of other distros out there,
# OE started sourcing /etc/profile in non-interactive bash shells. We set
# the global hard ulimits for stack sizes to 256 in /etc/profile which
# causes segmentation faults in the xfce environement for the embedded UI
# because UI apps (like thunar) require more than 256kb stack sizes.
# Disable sourcing /etc/profile in non-interactive shells (same behaviour
# we had in previous releases) until we remove the ulimit call from
# /etc/profile in 2018 (it's too late to do this in 2017 because it risks
# invalidating manual tests, it's the end of beta almost..).
CFLAGS_remove += "-DNON_INTERACTIVE_LOGIN_SHELLS"
