#!/bin/bash -e

fail() { echo "$@"; exit 1; }

SOURCEDIR="$1"
OUTDIR="$2"

[ -d "$SOURCEDIR" -a "$#" -eq 2 ] || \
    fail "Usage $0 <kernel source dir> <header export dir>"

[ -e "$SOURCEDIR"/Makefile -a -e "$SOURCEDIR"/Kconfig ] || \
	fail "Must run from within kernel tree."

mkdir -p "$OUTDIR"

cd "$SOURCEDIR"

# Copy Makefile, Kconfig files
rsync -avm \
      --chown=0:0 \
      --include="/arch/${ARCH}/" \
      --exclude="/arch/*/" \
      --exclude="/Documentation/" \
      --exclude="/scripts/" \
      --include="*/" \
      --include="Makefile*" \
      --include="Kconfig*" \
      --exclude="*" \
      . "$OUTDIR"

# Copy headers
rsync -avm \
      --chown=0:0 \
      --include="*/" \
      --include="/arch/${ARCH}/include/**" \
      --include="/arch/${ARCH}/mach-zynq/include/**" \
      --include="/include/**" \
      --include="/scripts/**" \
      --exclude="*" \
      . "$OUTDIR"

cd "${KBUILD_OUTPUT}"

# Copy generated headers and build objects
rsync -avm \
      --chown=0:0 \
      --include="*/" \
      --exclude="*.o"  \
      --exclude=".debug" \
      --exclude=".*.cmd" \
      --include="/arch/${ARCH}/include/generated/**" \
      --include="/include/**" \
      --include="/scripts/**${CROSS_COMPILE}*" \
      --include="/scripts/**.h" \
      --include=".config" \
      --include="Module.symvers" \
      --exclude="*" \
      . "$OUTDIR"

# Strip cross compile prefix from target-specific binaries
for u in recordmcount fixdep genksyms modpost; do
    find "$OUTDIR/scripts" -name "${CROSS_COMPILE}$u" -printf %h | \
	xargs -n 1 -I '{}' mv -f '{}'"/${CROSS_COMPILE}$u" '{}'"/$u"
done

# Remove VC-specific files
find "$OUTDIR" -name .gitignore -delete

chown -R 0:0 "$OUTDIR"
