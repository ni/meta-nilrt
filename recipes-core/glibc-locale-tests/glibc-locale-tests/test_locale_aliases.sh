#!/bin/bash
# SPDX-License-Identifier: MIT
set -euo pipefail

. /usr/lib/ptest-utils/bash/ptest-format.sh

# Expect these aliases on a Base System Image install.
EXPECTED_ALIASES=("CP932" "CP936" "L1")

expected_aliases_on_system() {
    readarray -t locales_and_aliases < <(locale -a | tr "\n" " ")
    readarray -t installed_locales < <(localedef --list-archive | tr "\n" " ")
    readarray -t aliases < <(echo ${locales_and_aliases[@]} ${installed_locales[@]} | tr " " "\n" | sort | uniq -u | tr "\n" " ")

    ptest_pass
    for alias in "${EXPECTED_ALIASES[@]}"; do
        alias_regex="\<$alias\>"
        if [[ ! ${aliases[@]} =~ $alias_regex ]]; then
            echo "ERROR: Expected alias $alias not found on system."
            ptest_fail
        fi
    done
}

ptest_test=$(basename -s ".sh" "$0")

ptest_change_subtest "1" "check for presence of expected aliases"
expected_aliases_on_system
ptest_report

exit $ptest_rc