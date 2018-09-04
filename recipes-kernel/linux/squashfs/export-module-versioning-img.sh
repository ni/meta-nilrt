#!/bin/bash -e
fail() { echo "$@"; exit 1; }

UTILS="recordmcount
fixdep
modpost
genksyms"

MKSQUASHFS="$1"
SRCDIR=`cd $2; pwd`
TMPDIR="$3"
INSTALLDIR="$4"
PREFIX="$5"

mkdir -p "$TMPDIR"
cp -a -f "$SRCDIR" "$TMPDIR"

# rename target tools to remove prefix
for U in $UTILS
do
    find "$TMPDIR" -name "$PREFIX$U" -printf %h | xargs -n 1 -I '{}' mv -f '{}'"/$PREFIX$U" '{}'"/$U"
done

"$MKSQUASHFS" "$TMPDIR" "$INSTALLDIR" -all-root -noappend
