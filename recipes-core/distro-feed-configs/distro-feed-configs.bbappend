# Must forward subfeed URIs variables to do_compile() in order to
#  correctly expand indirect references. These are distribution wide
#  variables that are defined in global conf(s).
# TODO: Is it possible to wildcard these `exports` so that we don't need
#  to update this recipe when new subfeeds are added?
export NIOE_SUBFEED_URI
export NIOEExtras_SUBFEED_URI
export NI_SUBFEED_URI
export NIOEMigration_SUBFEED_URI

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

inherit ptest

SRC_URI_append = " \
    file://run-ptest \
    file://ptest-format.sh \
    file://test_feed_listings.sh \
"

RDEPENDS_${PN}-ptest += " bash "

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

    # Load up the snowflakes in NILRT_ADDITIONAL_FEED_URIS
    for feedTuple in ${NILRT_ADDITIONAL_FEED_URIS}; do
        feedName=$(echo $feedTuple | sed 's/^[ \t]*\([^ \t#]*\)[ \t]*##.*$/\1/')
        feedUrl=$(echo $feedTuple | sed 's/^.*##[ \t]*\([^ \t]*\)[ \t]*.*$/\1/')
        echo "src/gz ${feedName} ${feedUrl}" > "${S}/${sysconfdir}/opkg/${feedName}-feed.conf"
    done
}

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
    install -m 755 ${WORKDIR}/test_feed_listings.sh ${D}${PTEST_PATH}
    install -m 644 ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    install -m 755 ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
}
