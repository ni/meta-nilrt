# Must forward subfeed URIs variables to do_compile() in order to
#  correctly expand indirect references. These are distribution wide
#  variables that are defined in global conf(s).
# TODO: Is it possible to wildcard these `exports` so that we don't need
#  to update this recipe when new subfeeds are added?
export NIOE_SUBFEED_URI
export NIOEExtras_SUBFEED_URI
export NI_SUBFEED_URI
export NIOEMigration_SUBFEED_URI

do_compile() {
    mkdir -p ${S}/${sysconfdir}/opkg

    # Add normal feeds
    for feedName in ${NILRT_SUBFEED_NAMES}; do
        for arch in ${NILRT_SUBFEED_ARCHES}; do
            distroFeedUri=`eval 'echo $'"${feedName}_SUBFEED_URI"`
            test -n "${distroFeedUri}"
            echo "src/gz ${feedName}-${arch} ${distroFeedUri}/${arch}" > "${S}/${sysconfdir}/opkg/${feedName}-${arch}-feed.conf"
        done
    done

    # Add NIOEMigration feed, which doesn't conform to the above
    # template. See distro conf for more details.
    test -n "${NIOEMigration_SUBFEED_URI}"
    echo "src/gz NIOEMigration-all ${NIOEMigration_SUBFEED_URI}/all" > "${S}/${sysconfdir}/opkg/NIOEMigration-all-feed.conf"
}
