# BSI HTTPS package feeds

Related work: AB#3795359; parent AB#3470397.

The BSI runmode payload and newly built safemode images use HTTPS opkg feeds.
The default root is `https://download.ni.com/ni-linux-rt/feeds`.

The feed layout is:

* `${release}/x64/{main,extra}/{all,x64,core2-64}/Packages.gz`
* `${release}/arm/{main,extra}/{all,xilinx-zynq,cortexa9-vfpv3}/Packages.gz`
* `${NILRT_MAIN_FEED_VERSION}/ni-main/Packages.gz`
* `dist/Packages.gz` (additional runmode feed)

Check the expanded `PACKAGE_FEED_ARCHS` for the selected machine rather than
assuming an architecture from another release. A feed directory may return 404
while its `Packages.gz` index is available. Check the index and referenced IPKs.
An unpublished release index is a publication blocker, not a TLS trust failure.
Do not silently substitute an older release.

## Build requirements

`nilrt-https-feeds.bbclass` rejects non-HTTPS network feed metadata before
`do_rootfs` and checks the generated `/etc/opkg/*.conf` source entries after
rootfs processing. Local `file://` entries in `IPK_FEED_URIS` remain valid
build-time inputs; remote entries must use HTTPS. Local build inputs are not
target feed configuration.

Local/site overrides of `NILRT_FEEDS_URI` must also use HTTPS with a certificate
trusted by the target. Changing the distro default does not override a value
already supplied in `site.conf`.

The base packagegroup requires `ca-certificates` in both safemode and runmode.
Opkg enables its libcurl certificate configuration and verifies peers.
Redirect following is disabled by default, so a feed
must serve its index and packages directly instead of redirecting to HTTP.
Do not set `ssl_dont_verify_peer` or `follow_location`, even to `0`: opkg enables
boolean options by their presence, ignoring the supplied value. The rootfs
check rejects either option. Do not add HTTP fallback feeds.

Build each architecture in its own initialized build directory:

```sh
MACHINE=x64 bitbake nilrt-base-system-image nilrt-safemode-rootfs
MACHINE=xilinx-zynq bitbake nilrt-base-system-image nilrt-safemode-rootfs
```

Inspect the runmode payload's `/etc/opkg/*.conf` and the corresponding safemode
rootfs. Run `bitbake -c validate_https_feeds nilrt-base-system-image` to test
metadata enforcement without building the image. Setting `NILRT_FEEDS_URI` or an
additional target feed to `http://` must fail this task.

Run the metadata regression tests in an initialized NILRT build environment:

```sh
oe-selftest -r nilrt_https_feeds
```

The `distro-feed-configs-ptest` feed-listing test also rejects HTTP sources on
the target.

## Safemode and device acceptance

The BSI uses the safemode already installed on the device; rebuilding a BSI does
not update that environment. Validate every supported installed safemode
version, including the oldest version allowed by the BSI CDF.

With the device clock set correctly, use the actual safemode CA bundle:

```sh
curl --cacert /etc/ssl/certs/ca-certificates.crt \
    --proto '=https' --proto-redir '=https' \
    https://download.ni.com/ni-linux-rt/feeds/dist/Packages.gz \
    -o /tmp/Packages.gz
```

Then install the BSI using the normal NI installation workflow and exercise the
actual opkg client in safemode and runmode:

```sh
opkg update
opkg install <test-package-from-the-selected-release>
```

Confirm the package version, HTTPS download URLs, and successful installation.
A host curl test using an extracted CA bundle only proves that bundle can
validate the current server chain; it does not prove device/NI installer
behavior, network access, or the trust stores in older safemode versions.

For the negative test, use an isolated test feed with a certificate issued by a
trusted test CA but already expired. Use fresh opkg list/cache directories so
cached content cannot mask failure. `opkg update` must exit nonzero with a
certificate-expiry error, and server logs must show no index or package request
over HTTP. Also test an HTTPS redirect to HTTP with a valid certificate; no HTTP
request should occur. Remove the test CA and restore the feed configuration
afterward. Never disable certificate validation to make these tests pass.
