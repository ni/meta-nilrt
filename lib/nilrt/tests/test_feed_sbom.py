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
from unittest.mock import patch

HERE = os.path.dirname(os.path.abspath(__file__))
META_NILRT_LIB = os.path.normpath(os.path.join(HERE, "..", ".."))
OE_LIB = os.path.normpath(os.path.join(META_NILRT_LIB, "..", "..", "openembedded-core", "meta", "lib"))
BITBAKE_LIB = os.path.normpath(os.path.join(META_NILRT_LIB, "..", "..", "bitbake", "lib"))
sys.path[:0] = [META_NILRT_LIB, OE_LIB, BITBAKE_LIB]

import bb.data  # noqa: E402
import oe.sbom30  # noqa: E402
import oe.spdx30  # noqa: E402
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

    def test_conflicting_feed_builds_fail(self):
        with tempfile.TemporaryDirectory() as directory:
            for index in range(2):
                with open(os.path.join(directory, str(index)), "w") as stream:
                    stream.write("Package: %s\nVersion: %s\nArchitecture: %s\nMD5Sum: %s\n" % (
                        NAME, VER, ARCH, str(index) * 32))
            with self.assertRaisesRegex(feed_sbom.FeedSbomError, "Conflicting"):
                feed_sbom.parse_opkg_lists(directory)


class ConvertTests(unittest.TestCase):
    def setUp(self):
        self.d = bb.data.init()
        for key, value in {
            "PN": "test-image", "BB_UNIHASH": "abcdef", "SPDX_UUID_NAMESPACE": "openembedded.org",
            "SPDX_NAMESPACE_PREFIX": "https://spdx.example", "SPDX_TOOL_NAME": "test",
            "SPDX_TOOL_VERSION": "1", "SPDX_AUTHORS": "", "SPDX_VERSION": "3.0.1",
            "SPDX_EXTERNAL_PACKAGE_FUNC": "",
        }.items():
            self.d.setVar(key, value)
        self.objset = oe.sbom30.ObjectSet.new_objset(self.d, "test", copy_from_bitbake_doc=False)
        author = self.objset.add(oe.spdx30.Organization(
            _id="https://spdx.example/author", name="Test author",
            creationInfo=self.objset.doc.creationInfo))
        self.objset.doc.creationInfo.createdBy.append(author)
        self.objset.doc.profileConformance = [oe.spdx30.ProfileIdentifierType.core]
        self.license_data = {
            "licenses": {name: {} for name in ("Zlib", "MIT", "Apache-2.0")},
            "licenseListVersion": "3.24",
        }

    def convert(self, cdx=None):
        return feed_sbom.cyclonedx_to_spdx30(
            self.objset, cdx or sample_cdx(), NAME, VER, ARCH,
            self.license_data, source_url=SBOM_URL)

    def serialize(self):
        buf = io.BytesIO()
        self.objset.objects.add(self.objset.doc)
        oe.spdx30.JSONLDInlineSerializer().write(self.objset, buf, force_at_graph=True)
        return json.loads(buf.getvalue())

    def test_main_package(self):
        package = self.convert()
        self.assertEqual(package.name, NAME)
        self.assertEqual(package.software_packageVersion, VER)
        self.assertEqual(package.software_primaryPurpose, oe.spdx30.software_SoftwarePurpose.install)
        self.assertEqual(package.software_downloadLocation, SBOM_URL[:-len(".cdx.sbom.json")])
        self.assertEqual(package.verifiedUsing[0].hashValue, MD5)
        self.assertEqual(package.suppliedBy.name, "NI")
        self.assertIn("architecture=core2-64", package.software_packageUrl)
        self.assertIn(SBOM_URL, package.software_sourceInfo)

    def test_components_and_licenses(self):
        self.convert()
        doc = self.serialize()
        self.assertEqual(doc["@context"], "https://spdx.org/rdf/3.0.1/spdx-context.jsonld")
        packages = list(self.objset.foreach_type(oe.spdx30.software_Package))
        self.assertEqual(len(packages), 6)
        self.assertEqual(len({p._id for p in packages}), 6)
        zlib = next(p for p in packages if p.name == "zlib")
        self.assertEqual(zlib.verifiedUsing[0].hashValue, "ab" * 32)
        self.assertEqual(zlib.externalIdentifier[0].externalIdentifierType,
                         oe.spdx30.ExternalIdentifierType.cpe23)
        expressions = list(self.objset.foreach_type(oe.spdx30.simplelicensing_LicenseExpression))
        self.assertEqual({p.simplelicensing_licenseExpression for p in expressions},
                         {"Zlib", "Apache-2.0 OR MIT"})
        main = next(p for p in packages if p.name == NAME)
        self.assertIn("National Instruments", main.comment)
        self.assertEqual(list(self.objset.foreach_type(oe.spdx30.simplelicensing_SimpleLicensingText)), [])
        parsed = oe.spdx30.SHACLObjectSet()
        oe.spdx30.JSONLDDeserializer().read(io.BytesIO(json.dumps(doc).encode()), parsed)
        self.assertEqual(len(list(parsed.foreach_type(oe.spdx30.software_Package))), 6)

    def test_nested_components_preserve_both_trees_and_parent_relationships(self):
        cdx = sample_cdx()
        cdx["components"] = [{"name": "top", "components": [{"name": "nested"}]}]
        cdx["metadata"]["component"]["components"] = [{"name": "metadata-child"}]
        self.convert(cdx)
        contains = {
            (r.from_.name, child.name)
            for r in self.objset.foreach_type(oe.spdx30.Relationship)
            if r.relationshipType == oe.spdx30.RelationshipType.contains
            for child in r.to
        }
        self.assertEqual(contains, {(NAME, "top"), ("top", "nested"), (NAME, "metadata-child")})

    def test_unnamed_nested_component_fails_instead_of_disappearing(self):
        cdx = sample_cdx()
        cdx["metadata"]["component"]["components"] = [
            {"name": "parent", "components": [{"version": "1"}]}]
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "under parent has no name"):
            self.convert(cdx)

    def test_unknown_license_is_preserved_without_inventing_full_text(self):
        cdx = sample_cdx()
        cdx["components"] = [{"name": "x", "version": "1", "licenses": [{"license": {"id": "NotAnSpdxId"}}]}]
        self.convert(cdx)
        package = next(p for p in self.objset.foreach_type(oe.spdx30.software_Package) if p.name == "x")
        self.assertIn("NotAnSpdxId", package.comment)
        self.assertEqual(list(self.objset.foreach_type(oe.spdx30.simplelicensing_SimpleLicensingText)), [])

    def test_supplied_full_license_text_is_preserved(self):
        cdx = sample_cdx()
        cdx["components"][0]["licenses"] = [{
            "license": {"name": "Custom", "text": {"content": "License text."}}}]
        self.convert(cdx)
        texts = list(self.objset.foreach_type(oe.spdx30.simplelicensing_SimpleLicensingText))
        self.assertEqual([lic.simplelicensing_licenseText for lic in texts], ["License text."])

    def test_base64_license_text_and_invalid_encoding(self):
        cdx = sample_cdx()
        license = {"name": "Custom", "text": {"encoding": "base64", "content": "TGljZW5zZQ=="}}
        cdx["components"][0]["licenses"] = [{"license": license}]
        self.convert(cdx)
        texts = list(self.objset.foreach_type(oe.spdx30.simplelicensing_SimpleLicensingText))
        self.assertEqual([lic.simplelicensing_licenseText for lic in texts], ["License"])
        license["text"]["content"] = "not valid base64!"
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "Invalid license text"):
            self.convert(cdx)

    def test_duplicate_references_and_unsupported_content_fail(self):
        cdx = sample_cdx()
        cdx["components"][0]["bom-ref"] = "duplicate"
        cdx["components"][1]["bom-ref"] = "duplicate"
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "Duplicate"):
            self.convert(cdx)
        cdx = sample_cdx()
        cdx["services"] = [{"name": "service"}]
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "services"):
            self.convert(cdx)

    def test_mismatched_metadata_fails_conversion(self):
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "version"):
            self.convert(sample_cdx(version="wrong"))

    def test_dependencies_and_unresolved_references(self):
        cdx = sample_cdx()
        cdx["metadata"]["component"]["bom-ref"] = "main"
        cdx["components"][0]["bom-ref"] = "child"
        cdx["dependencies"] = [{"ref": "main", "dependsOn": ["child"]}]
        self.convert(cdx)
        deps = list(self.objset.foreach_filter(
            oe.spdx30.Relationship, relationshipType=oe.spdx30.RelationshipType.dependsOn))
        self.assertEqual(len(deps), 1)
        self.assertEqual(deps[0].from_.name, NAME)
        self.assertEqual(deps[0].to[0].name, "CPUInfo")
        cdx["dependencies"][0]["dependsOn"] = ["missing"]
        with self.assertRaisesRegex(feed_sbom.FeedSbomError, "Unresolved"):
            self.convert(cdx)

    def test_external_hook_and_default_missing_provider_behavior(self):
        import oe.spdx30_tasks

        build = self.objset.new_task_build("rootfs", "rootfs")
        with patch("oe.spdx_common.collect_direct_deps", return_value=[]), \
                patch("oe.spdx_common.collect_package_providers", return_value={}), \
                patch("bb.note") as note:
            packages = {NAME: {"ver": VER, "arch": ARCH}}
            oe.spdx30_tasks.collect_build_package_inputs(self.d, self.objset, build, packages)
            note.assert_called_once()
            package = self.convert()
            self.d.setVar("SPDX_EXTERNAL_PACKAGE_FUNC", "external_package")
            with patch("bb.utils.better_eval", return_value=package):
                oe.spdx30_tasks.collect_build_package_inputs(self.d, self.objset, build, packages)
            inputs = list(self.objset.foreach_filter(
                oe.spdx30.LifecycleScopedRelationship,
                relationshipType=oe.spdx30.RelationshipType.hasInput))
            self.assertEqual(len(inputs), 1)
            self.assertIn(package._id, inputs[0].to)

    def test_recipe_provider_does_not_invoke_external_hook(self):
        import oe.spdx30_tasks

        package = self.convert()
        self.d.setVar("SPDX_EXTERNAL_PACKAGE_FUNC", "external_package")
        build = self.objset.new_task_build("rootfs", "rootfs")
        with patch("oe.spdx_common.collect_direct_deps", return_value=[]), \
                patch("oe.spdx_common.collect_package_providers", return_value={NAME: (NAME, "hash")}), \
                patch("oe.sbom30.find_root_obj_in_jsonld", return_value=(package, self.objset)), \
                patch("bb.utils.better_eval") as external:
            oe.spdx30_tasks.collect_build_package_inputs(
                self.d, self.objset, build, {NAME: {"ver": VER, "arch": ARCH}}, {})
            external.assert_not_called()

    def test_external_hook_must_return_a_package(self):
        import oe.spdx30_tasks

        self.d.setVar("SPDX_EXTERNAL_PACKAGE_FUNC", "external_package")
        build = self.objset.new_task_build("rootfs", "rootfs")
        with patch("oe.spdx_common.collect_direct_deps", return_value=[]), \
                patch("oe.spdx_common.collect_package_providers", return_value={}), \
                patch("bb.utils.better_eval", return_value=None), \
                patch("bb.fatal", side_effect=RuntimeError) as fatal:
            with self.assertRaises(RuntimeError):
                oe.spdx30_tasks.collect_build_package_inputs(
                    self.d, self.objset, build, {NAME: {"ver": VER, "arch": ARCH}})
            self.assertIn("did not return an SPDX package", fatal.call_args.args[0])


if __name__ == "__main__":
    unittest.main()
