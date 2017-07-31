FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-Fix-tyop-in-a-comment.patch \
            file://0002-Add-something-to-run-libcap-s-quicktest-tests-agains.patch \
            file://0003-Add-initial-support-for-the-ambient-set.patch \
            file://0004-Clean-up-ambient-support-and-add-a-smoke-test-for-th.patch \
"
