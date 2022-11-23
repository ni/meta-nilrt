
# The `wireless-regdb-static` subpackage installs static databases for wireless
# regulatory negotiation. NILRT uses CRDA for run-time negotiation, so these
# databse files are superfluous. They also CONFLICT with `wireless-regdb` and
# so need to be removed.
RDEPENDS:packagegroup-base-wifi:remove = "wireless-regdb-static"
RDEPENDS:packagegroup-base-wifi += "wireless-regdb"
