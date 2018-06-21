DEFAULT_PREFERENCE = "1"
# Need to use version 1.1.0 as in the nilrt 18.0 releases because the other tpm2-tools,
# tpm2-tss, etc. are using ni-internal forks with old branches. Once we rebase the other
# tpm2 repos we can also uprev this to a newer revision or drop it entirely if we move
# to the in-kernel resource manager.
SRCREV = "1003fcfaad39f5c27f75deb678f4fba253f38a82"
EXTRA_OEMAKE = 'CFLAGS="${CFLAGS} -I${STAGING_INCDIR}/sapi/"'

