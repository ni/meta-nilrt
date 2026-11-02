#
# Copyright (c) 2026 Emerson Electric Co.
#
# SPDX-License-Identifier: MIT
#
# Run from the nilrt repo root:
#   python3 sources/meta-nilrt/lib/nilrt/tests/test_feed_sbom.py
#
import io
import json
import os
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
META_NILRT_LIB = os.path.normpath(os.path.join(HERE, "..", ".."))
OE_LIB = os.path.normpath(os.path.join(META_NILRT_LIB, "..", "..", "openembedded-core", "meta", "lib"))
sys.path[:0] = [META_NILRT_LIB, OE_LIB]

import oe.spdx  # noqa: E402
from nilrt import feed_sbom  # noqa: E402

POOL = "http://pool.example/ni/linuxpkg/pool/ipk"
NAME, VER, ARCH = "ni-sysapi", "27.0.0.60-0+d60", "core2-64"
MD5 = "b5063f83be75eb9d04be3a5e5c6f673c"
VDIR = POOL + "/ni-s/ni-sysapi/27.0.0/27.0.0.60-0%2Bd60/"
SBOM_URL = VDIR + "8a10e89c/ni-sysapi_27.0.0.60-0%2Bd60_core2-64.ipk.cdx.sbom.json"


def listing(*hrefs):
    links = "".join('<A HREF="%s">%s</A><br>' % (h, h.rstrip("/").split("/")[-1]) for h in hrefs)
    return ('<html><body><A HREF="/ni/linuxpkg/pool/">[To Parent Directory]</A><br>%s</body></html>' % links).encode()


def sample_cdx(name=NAME, version=VER, arch=ARCH, md5=MD5):
    return {
        "bomFormat": "CycloneDX",
        "specVersion": "1.6",
        "serialNumber": "urn:uuid:f3d01351-29e0-47fa-8c37-4df86d0531a2",
        "metadata": {
            "component": {
                "type": "library",
                "name": name,
                "version": version,
                "purl": "pkg:generic/ipk/%s@%s?architecture=%s" % (name, version.replace("+", "%2B"), arch),
                "supplier": {"name": "NI"},
                "copyright": "National Instruments, Inc. All rights reserved.",
                "hashes": [{"alg": "MD5", "content": md5}],
                "licenses": [{"license": {"name": "National Instruments, Inc.",
                                          "url": "https://www.ni.com/en/about-ni/legal/software-license-agreement.html"}}],
            },
        },
        "components": [
            {"type": "library", "name": "CPUInfo", "version": "16.0.0f1", "supplier": {"name": "NI"},
             "licenses": [{"license": {"name": "National Instruments, Inc."}}]},
            {"type": "library", "name": "LVRTEthernet", "version": "2.0.0b3"},
            {"type": "library", "name": "LVRTEthernet", "version": "2.0.0f0"},
            {"type": "library", "name": "zlib", "version": "1.3.1", "purl": "pkg:generic/zlib@1.3.1",
             "cpe": "cpe:2.3:a:zlib:zlib:1.3.1:*:*:*:*:*:*:*",
             "licenses": [{"license": {"id": "Zlib"}}],
             "hashes": [{"alg": "SHA-256", "content": "AB" * 32}]},
            {"type": "library", "name": "openssl", "version": "3.0.13",
             "licenses": [{"expression": "Apache-2.0 OR MIT"}]},
        ],
    }


class FakePool:
    """Serves the pool layout seen on argohttp: one version folder with one
    build-hash folder per architecture."""

    def __init__(self, cdx=None):
        self.files = {
            POOL + "/": listing("/ni/linuxpkg/pool/ipk/ni-s/", "/ni/linuxpkg/pool/ipk/nirt/"),
            VDIR: listing("/ni/linuxpkg/pool/ipk/ni-s/ni-sysapi/27.0.0/27.0.0.60-0%2Bd60/59205b1e/",
                          "/ni/linuxpkg/pool/ipk/ni-s/ni-sysapi/27.0.0/27.0.0.60-0%2Bd60/8a10e89c/"),
            SBOM_URL: json.dumps(cdx or sample_cdx()).encode(),
        }
        self.requests = []

    def get(self, url):
        self.requests.append(url)
        return self.files.get(url)


class PathTests(unittest.TestCase):
    def test_version_series(self):
        self.assertEqual(feed_sbom.version_series("27.0.0.60-0+d60"), "27.0.0")
        self.assertEqual(feed_sbom.version_series("26.3.0.49300-0+f148"), "26.3.0")
        self.assertEqual(feed_sbom.version_series("1:20.0.0.49152-0+f0"), "20.0.0")
        with self.assertRaises(feed_sbom.FeedSbomError):
            feed_sbom.version_series("git-r0")

    def test_version_dir_url_quotes_plus(self):
        self.assertEqual(feed_sbom.version_dir_url(POOL, "ni-s", NAME, VER), VDIR)

    def test_list_subdirs_ignores_parent_and_other_paths(self):
        html = listing("/ni/linuxpkg/pool/ipk/ni-s/ni-sysapi/27.0.0/27.0.0.60-0%2Bd60/8a10e89c/").decode()
        self.assertEqual(feed_sbom.list_subdirs(html, VDIR), ["8a10e89c"])
        self.assertEqual(feed_sbom.list_subdirs(html, POOL + "/other/"), [])


class FetchTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.cache = os.path.join(self.tmp.name, "cache")

    def tearDown(self):
        self.tmp.cleanup()

    def test_finds_matching_arch_in_build_hash_folders(self):
        pool = FakePool()
        self.assertEqual(feed_sbom.find_sbom_url(POOL, NAME, VER, ARCH, get=pool.get), SBOM_URL)

    def test_fetch_downloads_then_uses_cache(self):
        pool = FakePool()
        path, url = feed_sbom.fetch_sbom(POOL, self.cache, NAME, VER, ARCH, get=pool.get)
        self.assertEqual(url, SBOM_URL)
        with open(path) as f:
            self.assertEqual(json.load(f)["metadata"]["component"]["name"], NAME)
        offline = FakePool()
        path2, url2 = feed_sbom.fetch_sbom(POOL, self.cache, NAME, VER, ARCH, allow_network=False, get=offline.get)
        self.assertEqual((path2, url2), (path, SBOM_URL))
        self.assertEqual(offline.requests, [])

    def test_selects_later_build_matching_feed_md5(self):
        pool = FakePool()
        first_url = SBOM_URL.replace("8a10e89c", "59205b1e")
        pool.files[first_url] = json.dumps(sample_cdx(md5="0" * 32)).encode()
        path, url = feed_sbom.fetch_sbom(
            POOL, self.cache, NAME, VER, ARCH, md5=MD5, get=pool.get)
        self.assertEqual(url, SBOM_URL)
        self.assertIn(first_url, pool.requests)
        self.assertEqual(pool.requests.count(SBOM_URL), 1)
        with open(path) as f:
            self.assertEqual(feed_sbom.verify_sbom(json.load(f), NAME, VER, ARCH, MD5), [])

    def test_replaces_cached_build_when_feed_md5_changes(self):
        pool = FakePool()
        path, _ = feed_sbom.fetch_sbom(
            POOL, self.cache, NAME, VER, ARCH, md5=MD5, get=pool.get)
        replacement_md5 = "1" * 32
        pool.files[SBOM_URL] = json.dumps(sample_cdx(md5=replacement_md5)).encode()
        path2, _ = feed_sbom.fetch_sbom(
            POOL, self.cache, NAME, VER, ARCH, md5=replacement_md5, get=pool.get)
        self.assertEqual(path2, path)
        with open(path2) as f:
            self.assertEqual(feed_sbom.verify_sbom(
                json.load(f), NAME, VER, ARCH, replacement_md5), [])

    def test_matching_cache_is_verified_and_used_offline(self):
        pool = FakePool()
        expected = feed_sbom.fetch_sbom(
            POOL, self.cache, NAME, VER, ARCH, md5=MD5, get=pool.get)
        pool.requests.clear()
        self.assertEqual(feed_sbom.fetch_sbom(
            POOL, self.cache, NAME, VER, ARCH, allow_network=False,
            md5=MD5, get=pool.get), expected)
        self.assertEqual(pool.requests, [])

    def test_mismatched_cache_fails_offline_without_network(self):
        pool = FakePool()
        feed_sbom.fetch_sbom(POOL, self.cache, NAME, VER, ARCH, get=pool.get)
        pool.requests.clear()
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "MD5.*BB_NO_NETWORK"):
            feed_sbom.fetch_sbom(
                POOL, self.cache, NAME, VER, ARCH, allow_network=False,
                md5="1" * 32, get=pool.get)
        self.assertEqual(pool.requests, [])

    def test_no_matching_build_is_not_cached(self):
        pool = FakePool()
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "Rejected candidates:"):
            feed_sbom.fetch_sbom(
                POOL, self.cache, NAME, VER, ARCH, md5="1" * 32, get=pool.get)
        self.assertFalse(os.path.exists(self.cache))

    def test_offline_without_cache_fails(self):
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "BB_NO_NETWORK"):
            feed_sbom.fetch_sbom(POOL, self.cache, NAME, VER, ARCH, allow_network=False, get=FakePool().get)

    def test_missing_version_fails(self):
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "No ni-sysapi_27.0.0.99"):
            feed_sbom.fetch_sbom(POOL, self.cache, NAME, "27.0.0.99-0+d99", ARCH, get=FakePool().get)

    def test_invalid_json_is_rejected_and_not_cached(self):
        pool = FakePool()
        pool.files[SBOM_URL] = b"<html>error</html>"
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "not valid JSON"):
            feed_sbom.fetch_sbom(POOL, self.cache, NAME, VER, ARCH, get=pool.get)
        self.assertFalse(os.path.exists(os.path.join(self.cache, feed_sbom.sbom_filename(NAME, VER, ARCH))))


class VerifyTests(unittest.TestCase):
    def test_matching_sbom(self):
        self.assertEqual(feed_sbom.verify_sbom(sample_cdx(), NAME, VER, ARCH, MD5.upper()), [])

    def test_mismatches_are_reported(self):
        problems = feed_sbom.verify_sbom(sample_cdx(version="27.0.0.80-0+d80", arch="cortexa9-vfpv3"),
                                         NAME, VER, ARCH, "0" * 32)
        self.assertEqual(len(problems), 3, problems)
        self.assertTrue(any("version" in p for p in problems))
        self.assertTrue(any("architecture" in p for p in problems))
        self.assertTrue(any("MD5" in p for p in problems))

    def test_missing_arch_or_md5_evidence_is_rejected(self):
        cdx = sample_cdx()
        comp = cdx["metadata"]["component"]
        comp["purl"] = "pkg:generic/ipk/%s@%s" % (NAME, VER)
        del comp["hashes"]
        problems = feed_sbom.verify_sbom(cdx, NAME, VER, ARCH, MD5)
        self.assertEqual(len(problems), 2, problems)
        # without a feed MD5 there is nothing to compare, so only the arch fails
        self.assertEqual(len(feed_sbom.verify_sbom(cdx, NAME, VER, ARCH)), 1)

    def test_parse_opkg_lists(self):
        with tempfile.TemporaryDirectory() as d:
            with open(os.path.join(d, "NI-main-software"), "w") as f:
                f.write("Package: ni-sysapi\nVersion: %s\nArchitecture: %s\nDescription: x\n continued: line\n"
                        "MD5Sum: %s\n\nPackage: other\nVersion: 1\nArchitecture: all\n" % (VER, ARCH, MD5.upper()))
            self.assertEqual(feed_sbom.parse_opkg_lists(d), {(NAME, VER, ARCH): MD5})
        self.assertEqual(feed_sbom.parse_opkg_lists("/does/not/exist"), {})


class ConvertTests(unittest.TestCase):
    def convert(self, cdx=None):
        ns = feed_sbom.document_namespace("http://spdx.org/spdxdocs", "openembedded.org", NAME, VER, ARCH)
        doc = feed_sbom.cyclonedx_to_spdx(
            oe.spdx, cdx or sample_cdx(), NAME, VER, ARCH, ns, "2026-10-30T00:00:00Z",
            source_url=SBOM_URL, license_ids=["Zlib", "MIT", "Apache-2.0"], license_list_version="3.24")
        buf = io.BytesIO()
        doc.to_json(buf, sort_keys=True)
        return json.loads(buf.getvalue())

    def test_namespace_is_stable_and_version_specific(self):
        a = feed_sbom.document_namespace("http://spdx.org/spdxdocs", "x", NAME, VER, ARCH)
        self.assertEqual(a, feed_sbom.document_namespace("http://spdx.org/spdxdocs", "x", NAME, VER, ARCH))
        self.assertNotEqual(a, feed_sbom.document_namespace("http://spdx.org/spdxdocs", "x", NAME, "1-r0", ARCH))
        self.assertTrue(a.startswith("http://spdx.org/spdxdocs/ni-sysapi-"))

    def test_main_package(self):
        doc = self.convert()
        self.assertEqual(doc["spdxVersion"], "SPDX-2.2")
        self.assertEqual(doc["name"], NAME)
        main = doc["packages"][0]
        self.assertEqual(main["name"], NAME)
        self.assertEqual(main["versionInfo"], VER)
        self.assertEqual(main["supplier"], "Organization: NI")
        self.assertEqual(main["packageFileName"], "ni-sysapi_27.0.0.60-0+d60_core2-64.ipk")
        self.assertEqual(main["downloadLocation"], SBOM_URL[:-len(".cdx.sbom.json")])
        self.assertEqual(main["checksums"], [{"algorithm": "MD5", "checksumValue": MD5}])
        self.assertEqual(main["licenseDeclared"], "LicenseRef-National-Instruments-Inc")
        self.assertEqual(main["externalRefs"][0]["referenceType"], "purl")
        self.assertIn({"spdxElementId": "SPDXRef-DOCUMENT", "relationshipType": "DESCRIBES",
                       "relatedSpdxElement": main["SPDXID"]}, doc["relationships"])
        self.assertIn(SBOM_URL, doc["creationInfo"]["comment"])

    def test_components_and_licenses(self):
        doc = self.convert()
        ids = [p["SPDXID"] for p in doc["packages"]]
        self.assertEqual(len(set(ids)), 6, "SPDXIDs must be unique even for duplicate component names")
        contains = [r for r in doc["relationships"] if r["relationshipType"] == "CONTAINS"]
        self.assertEqual(len(contains), 5)
        zlib = next(p for p in doc["packages"] if p["name"] == "zlib")
        self.assertEqual(zlib["licenseDeclared"], "Zlib")
        self.assertEqual({r["referenceType"] for r in zlib["externalRefs"]}, {"purl", "cpe23Type"})
        self.assertEqual(zlib["checksums"][0]["algorithm"], "SHA256")
        openssl = next(p for p in doc["packages"] if p["name"] == "openssl")
        self.assertEqual(openssl["licenseDeclared"], "Apache-2.0 OR MIT")
        lvrt = [p for p in doc["packages"] if p["name"] == "LVRTEthernet"]
        self.assertEqual([p["supplier"] for p in lvrt], ["NOASSERTION", "NOASSERTION"])
        refs = [l["licenseId"] for l in doc["hasExtractedLicensingInfos"]]
        self.assertEqual(refs, ["LicenseRef-National-Instruments-Inc"])

    def test_nested_components_preserve_both_trees_and_parent_relationships(self):
        cdx = sample_cdx()
        cdx["components"] = [{
            "name": "top", "version": "1",
            "components": [{
                "name": "nested", "version": "2",
                "components": [{
                    "name": "leaf", "version": "3",
                    "licenses": [{"license": {"name": "Nested License"}}],
                    "purl": "pkg:generic/leaf@3",
                    "hashes": [{"alg": "SHA-256", "content": "AB" * 32}],
                }],
            }],
        }]
        cdx["metadata"]["component"]["components"] = [{
            "name": "metadata-child", "version": "4",
            "components": [{"name": "nested", "version": "5"}],
        }]
        doc = self.convert(cdx)
        packages = {(p["name"], p["versionInfo"]): p for p in doc["packages"]}
        self.assertEqual(len(packages), 6)
        ids = {p["SPDXID"] for p in doc["packages"]}
        self.assertEqual(len(ids), 6)
        by_id = {p["SPDXID"]: (p["name"], p["versionInfo"]) for p in doc["packages"]}
        contains = {
            (by_id[r["spdxElementId"]], by_id[r["relatedSpdxElement"]])
            for r in doc["relationships"] if r["relationshipType"] == "CONTAINS"
        }
        self.assertEqual(contains, {
            ((NAME, VER), ("top", "1")),
            (("top", "1"), ("nested", "2")),
            (("nested", "2"), ("leaf", "3")),
            ((NAME, VER), ("metadata-child", "4")),
            (("metadata-child", "4"), ("nested", "5")),
        })
        leaf = packages[("leaf", "3")]
        self.assertEqual(leaf["licenseDeclared"], "LicenseRef-Nested-License")
        self.assertIn("LicenseRef-Nested-License",
                      {info["licenseId"] for info in doc["hasExtractedLicensingInfos"]})
        self.assertEqual(leaf["externalRefs"][0]["referenceLocator"], "pkg:generic/leaf@3")
        self.assertEqual(leaf["checksums"][0]["checksumValue"], "ab" * 32)
        parsed = oe.spdx.SPDXDocument.from_json(io.StringIO(json.dumps(doc)))
        self.assertEqual(len(parsed.packages), 6)

    def test_unnamed_nested_component_fails_instead_of_disappearing(self):
        cdx = sample_cdx()
        cdx["metadata"]["component"]["components"] = [
            {"name": "parent", "components": [{"version": "1"}]}]
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "under parent has no name"):
            self.convert(cdx)

    def test_unknown_license_id_becomes_licenseref(self):
        cdx = sample_cdx()
        cdx["components"] = [{"name": "x", "version": "1", "licenses": [{"license": {"id": "NotAnSpdxId"}}]}]
        doc = self.convert(cdx)
        self.assertEqual(doc["packages"][1]["licenseDeclared"], "LicenseRef-NotAnSpdxId")

    def test_expression_licenserefs_are_declared(self):
        cdx = sample_cdx()
        cdx["components"] = [{"name": "x", "version": "1", "licenses": [
            {"expression": "(MIT OR LicenseRef-Custom) AND NotAnSpdxId AND Apache-2.0 WITH LLVM-exception"}]}]
        doc = self.convert(cdx)
        self.assertEqual(doc["packages"][1]["licenseDeclared"],
                         "(MIT OR LicenseRef-Custom) AND LicenseRef-NotAnSpdxId AND Apache-2.0 WITH LLVM-exception")
        refs = {l["licenseId"] for l in doc["hasExtractedLicensingInfos"]}
        self.assertTrue({"LicenseRef-Custom", "LicenseRef-NotAnSpdxId"} <= refs, refs)

    def test_document_reads_back_with_oe_spdx(self):
        doc = self.convert()
        parsed = oe.spdx.SPDXDocument.from_json(io.StringIO(json.dumps(doc)))
        self.assertEqual(parsed.packages[0].name, NAME)


if __name__ == "__main__":
    unittest.main()
