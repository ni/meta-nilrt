meta-nilrt
=====

This layer provides support for the NI Linux Real-Time distribution. The NI Linux Real-Time distribution supplies a real-time operating system that runs on National Instruments RT targets, such as NI CompactRIO, and provides the flexibility and customization benefits of a Linux system.

About NI Linux Real-Time
------------------------
To learn about NI Linux Real-Time, visit the [NI Linux Real-Time Portal].

Building the Layer
-----------

To build the meta-nilrt layer, follow the instructions available on [Github].

Contact
----
Email [licensing@emerson.com]

Feed package SBOMs
-----------------
This integration requires SPDX 3.0 to be enabled and the companion
OE-core `SPDX_EXTERNAL_PACKAGE_FUNC` image hook. The SPDX 3.0 default
switch is a separate prerequisite (Jatin's `jatin/spdx_change` branch);
it is not included in this change. SPDX 2.2 and SDK integration are not
provided by this implementation.

For SPDX 3.0 image builds, `nilrt-feed-spdx` includes inventory from the
CycloneDX SBOMs published alongside prebuilt feed IPKs. Packages without a
local SPDX provider must have a pool SBOM matching their installed name,
version, architecture and feed IPK MD5; otherwise the image SBOM task fails.
Nested components, containment and declared dependency relationships are
included in the consolidated image `.spdx.json`. No separate package SPDX
archive is generated.
License names/URLs without full text are preserved as source declarations
in package comments, with no assertion of license text. Supplied full text
and SPDX license expressions use the corresponding SPDX licensing objects.

`NILRT_FEED_SBOM_POOL_URI` selects the package pool and
`NILRT_FEED_SBOM_CACHE` defaults to `${DL_DIR}/nilrt-feed-sbom`. For offline
builds, populate the cache with `<name>_<version>_<arch>.ipk.cdx.sbom.json`
files and set `BB_NO_NETWORK = "1"`. Cached entries are validated too.
Feed builds are cached under `<IPK-MD5>/<name>_<version>_<arch>.ipk.cdx.sbom.json`
so distinct builds cannot replace each other's cache entries. Existing flat
cache files are migrated only after matching the expected feed MD5.
Package licenses come only from component-level declarations; BOM document
licenses are retained separately in a source-metadata annotation.
SHA-3 hashes and both CPE 2.2 URI and CPE 2.3 formatted bindings are preserved.
The default internal pool uses HTTP: MD5 selects the matching IPK build,
but does not authenticate the SBOM contents. Use a trusted network/cache
or an authenticated pool endpoint.

Run the focused tests from the NILRT repository root:
`python3 sources/meta-nilrt/lib/nilrt/tests/test_feed_sbom.py`.

[NI Linux Real-Time Portal]:http://www.ni.com/white-paper/14627/en/
[Github]:https://www.github.com/ni/nilrt
[licensing@emerson.com]:mailto:licensing@emerson.com
