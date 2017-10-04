#!/bin/bash

. $(dirname $0)/ptest-format.sh

# Get a listing of files with feed listings
FEED_CONF_FILES=$(grep -lE "^[ \t]*src/gz" /etc/opkg/*.conf)

ptest_test=$(basename $0 .sh)
ptest_num=0

PASS=1
for conf_file in ${FEED_CONF_FILES}; do

# Find all listings of feeds for the current file, ensure that they
# adhere to the strict formatting requirements of opkg.
# The sed regexes here enforce strict formating.
#
# The flow goes:
# > All lines that start [0+ spaces]src/gz in /etc/opkg/*.conf files
# >> Replace any line that follows the strict format:
# >>> ^src/gz ([1+ non-space])( https?://[1+ non-space])$
# >> with the line
# >>> pass - [first group, the feed name]
# >> Pass the result of this replacement to the following sed
# >>> ^[0+ spaces]src/gz[1+ spaces]([1+ non-space])[rest of line]$
# >> with the line
# >>> fail - [first group, the feed name]
#
# While this leads to a real doozy of a oneliner, this is done to enforce
# strict formatting checks, as formatting can be lost when passing variables
# around in bash
    CUR_FEED_LISTINGS="$(grep -hE "^[ \t]*src/gz" ${conf_file} | sed 's#^src/gz \([^ \t]\+\)\( https\?://[^ \t]\+\)$#pass - \1#' | sed 's#^[ \t]*src/gz[ \t]\+\([^ \t]\+\).*$#fail - \1#'| sed 's#^\(.*src/gz.*\)$#fail - UNKNOWN REPO "\1\"#')"

    while read -r line; do
        echo $line

        # Check the string starts with "fail"
        # the above regex enforces formatting
        [ "${line%% *}" = "fail" ] && PASS=0
    done <<< "${CUR_FEED_LISTINGS}"
done

[ $PASS -eq 1 ] && ptest_pass
ptest_report
