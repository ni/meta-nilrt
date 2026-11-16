SUMMARY = "Linux kernel NO_HZ_FULL polling test"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://nohz_test.c;beginline=1;endline=2;md5=9e3e9401383732750e93a18064dc9493"

inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"

S = "${UNPACKDIR}"

RDEPENDS:${PN}-ptest += "bash coreutils procps rt-tests"
RDEPENDS:${PN}-ptest:append:x64 = " packagegroup-ni-nohz-kernel"
ALLOW_EMPTY:${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://nohz_test.c \
    file://PXIe-8880.conf \
    file://PXIe-8881.conf \
"

LDFLAGS += "-lpthread"

do_compile_ptest:append() {
    cd ${S}
    ${CC} ${CFLAGS} -o nohz_test nohz_test.c ${LDFLAGS}
}

do_install_ptest:append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/nohz_test ${D}${PTEST_PATH}
    install -m 0644 ${S}/PXIe-8880.conf ${D}${PTEST_PATH}
    install -m 0644 ${S}/PXIe-8881.conf ${D}${PTEST_PATH}
}

pkg_postinst_ontarget:${PN}-ptest:append() {
    CPUS=`nproc`
    ISOLATED_CPU=$((CPUS - 1))

    if [ $ISOLATED_CPU -le 0 ]; then
        echo "[kernel-test-nohz:error] This test requires a system with 2 or more CPUs"
        exit 1
    fi

    NOHZ_CFG='set otherbootargs="${otherbootargs} isolcpus=nohz,domain,managed_irq,'$ISOLATED_CPU' nohz_full='$ISOLATED_CPU' mitigations=off intel_pstate=disable intel_idle.max_cstate=0 processor.max_cstate=0 nosoftlockup mce=ignore_ce audit=0 tsc=reliable"'

    NOHZ_DROPIN=/boot/runmode/bootimage.cfg.d/nohz.cfg
    sb_var=/sys/firmware/efi/efivars/SecureBoot-8be4df61-93ca-11d2-aa0d-00e098032b8c
    SECURE_BOOT_ENABLED=0
    if [ -r "$sb_var" ] && [ "$(od -An -j4 -N1 -tu1 "$sb_var" | tr -d '[:space:]')" = "1" ]; then
        SECURE_BOOT_ENABLED=1
    fi

    RUNMODE_CFG_SIGNED=0
    for sig_ext in .sig .p7b; do
        if [ -e "/boot/runmode/bootimage.cfg${sig_ext}" ]; then
            RUNMODE_CFG_SIGNED=1
        fi
    done

    NOHZ_DROPIN_INSTALLED=0
    for file in "$NOHZ_DROPIN" "$NOHZ_DROPIN.sig" "$NOHZ_DROPIN.p7b"; do
        if [ -e "$file" ]; then
            NOHZ_DROPIN_INSTALLED=1
        fi
    done

    if [ "$SECURE_BOOT_ENABLED" = "1" ] || [ "$RUNMODE_CFG_SIGNED" = "1" ] || [ "$NOHZ_DROPIN_INSTALLED" = "1" ]; then
        # Preserve signed runmode configs and stage changes as a signed drop-in.
        mkdir -p /var/lib/kernel-test-nohz
        echo "$NOHZ_CFG" > /var/lib/kernel-test-nohz/nohz.cfg
        if [ "$SECURE_BOOT_ENABLED" = "1" ]; then
            echo "[kernel-test-nohz:info] Secure Boot is enabled."
        fi
        if [ "$RUNMODE_CFG_SIGNED" = "1" ]; then
            echo "[kernel-test-nohz:info] A signed runmode bootimage.cfg is installed."
        fi
        if [ "$NOHZ_DROPIN_INSTALLED" = "1" ]; then
            echo "[kernel-test-nohz:info] A nohz runmode drop-in or signature is already installed."
        fi
        echo "[kernel-test-nohz:info] Manual steps are required before reboot:"
        echo "[kernel-test-nohz:info]   1. Copy /var/lib/kernel-test-nohz/nohz.cfg off the target and sign it with the release signing key (same format as the signed bootimage drop-ins)."
        echo "[kernel-test-nohz:info]   2. Copy the signed nohz.cfg and its signature to /boot/runmode/bootimage.cfg.d/ on the target."
        echo "[kernel-test-nohz:info]   3. Run ni-reseal-luks so the PCR 15 seal matches the new drop-in."
        echo "[kernel-test-nohz:info]   4. Reboot."
        exit 0
    fi

    echo "$NOHZ_CFG" > /boot/runmode/no-hz-full-params.cfg
    grep -qsxF 'source /runmode/no-hz-full-params.cfg' /boot/runmode/bootimage.cfg || echo 'source /runmode/no-hz-full-params.cfg' >> /boot/runmode/bootimage.cfg

    echo "[kernel-test-nohz:info] This test requires a reboot after install for kernel changes to take effect"
}

pkg_postrm:${PN}-ptest:append() {
    rm -rf /var/lib/kernel-test-nohz
    NOHZ_DROPIN=/boot/runmode/bootimage.cfg.d/nohz.cfg
    sb_var=/sys/firmware/efi/efivars/SecureBoot-8be4df61-93ca-11d2-aa0d-00e098032b8c
    SECURE_BOOT_ENABLED=0
    if [ -r "$sb_var" ] && [ "$(od -An -j4 -N1 -tu1 "$sb_var" | tr -d '[:space:]')" = "1" ]; then
        SECURE_BOOT_ENABLED=1
    fi

    NOHZ_DROPIN_INSTALLED=0
    for file in "$NOHZ_DROPIN" "$NOHZ_DROPIN.sig" "$NOHZ_DROPIN.p7b"; do
        if [ -e "$file" ]; then
            NOHZ_DROPIN_INSTALLED=1
        fi
    done
    if [ "$NOHZ_DROPIN_INSTALLED" = "1" ]; then
        echo "[kernel-test-nohz:info] A nohz runmode drop-in or signature is installed. Remove /boot/runmode/bootimage.cfg.d/nohz.cfg and its signature, run ni-reseal-luks, then reboot."
        exit 0
    fi

    RUNMODE_CFG_SIGNED=0
    for sig_ext in .sig .p7b; do
        if [ -e "/boot/runmode/bootimage.cfg${sig_ext}" ]; then
            RUNMODE_CFG_SIGNED=1
        fi
    done
    if grep -qsxF 'source /runmode/no-hz-full-params.cfg' /boot/runmode/bootimage.cfg &&
       { [ "$SECURE_BOOT_ENABLED" = "1" ] || [ "$RUNMODE_CFG_SIGNED" = "1" ]; }; then
        if [ "$SECURE_BOOT_ENABLED" = "1" ]; then
            echo "[kernel-test-nohz:info] Secure Boot is enabled."
        fi
        if [ "$RUNMODE_CFG_SIGNED" = "1" ]; then
            echo "[kernel-test-nohz:info] A signed runmode bootimage.cfg is installed."
        fi
        echo "[kernel-test-nohz:info] Remove the test source line from /boot/runmode/bootimage.cfg and re-sign it if required, then run ni-reseal-luks and reboot."
        exit 0
    fi

    sed -i '/^source \/runmode\/no-hz-full-params\.cfg/d' /boot/runmode/bootimage.cfg
    rm -f /boot/runmode/no-hz-full-params.cfg
    echo "[kernel-test-nohz:info] This test requires a reboot after uninstall for kernel changes to take effect"
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
