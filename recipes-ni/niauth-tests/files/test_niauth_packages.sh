#!/bin/bash
# Verifies that ni-auth related packages are not installed.

source "$(dirname "$0")/ptest-format.sh"

ptest_test=$(basename "$0" ".sh")  # test_niauth_packages


# TEST: ni-auth is not installed
function test_ni_auth_not_installed() {
    ptest_pass
    if opkg list-installed | grep -q "^ni-auth "; then
        ptest_fail
    fi
}

# TEST: pam-plugin-niauth is not installed
function test_pam_plugin_niauth_not_installed() {
    ptest_pass
    if opkg list-installed | grep -q "^pam-plugin-niauth "; then
        ptest_fail
    fi
}

# TEST: libnss-niauth is not installed
function test_libnss_niauth_not_installed() {
    ptest_pass
    if opkg list-installed | grep -q "^libnss-niauth "; then
        ptest_fail
    fi
}

# TEST: ni-auth-networkcontroller is not installed
function test_ni_auth_networkcontroller_not_installed() {
    ptest_pass
    if opkg list-installed | grep -q "^ni-auth-networkcontroller "; then
        ptest_fail
    fi
}


ptest_change_subtest 1 "ni-auth not installed"
test_ni_auth_not_installed
ptest_report

ptest_change_subtest 2 "pam-plugin-niauth not installed"
test_pam_plugin_niauth_not_installed
ptest_report

ptest_change_subtest 3 "libnss-niauth not installed"
test_libnss_niauth_not_installed
ptest_report

ptest_change_subtest 4 "ni-auth-networkcontroller not installed"
test_ni_auth_networkcontroller_not_installed
ptest_report
