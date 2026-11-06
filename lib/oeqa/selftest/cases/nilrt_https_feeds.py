from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import bitbake


class NilrtHttpsFeeds(OESelftestTestCase):
    def test_https_target_feeds_allow_local_build_inputs(self):
        self.write_config(
            'NILRT_FEEDS_URI = "https://example.invalid/feeds"\n'
            'IPK_FEED_URIS = "local##file:///tmp/nilrt-feed"\n'
        )
        bitbake(
            "-c validate_https_feeds nilrt-base-system-image "
            "nilrt-runmode-rootfs nilrt-safemode-initramfs"
        )

    def check_rejected_feed(self, setting, source):
        self.write_config(
            'NILRT_FEEDS_URI = "https://example.invalid/feeds"\n' + setting + "\n"
        )
        result = bitbake(
            "-c validate_https_feeds nilrt-base-system-image", ignore_status=True
        )
        self.assertNotEqual(result.status, 0)
        self.assertIn(source + " must be an HTTPS feed URI", result.output)

    def test_http_root_rejected(self):
        self.check_rejected_feed(
            'NILRT_FEEDS_URI = "http://example.invalid/feeds"', "NILRT_FEEDS_URI"
        )

    def test_http_base_feed_rejected(self):
        self.check_rejected_feed(
            'PACKAGE_FEED_URIS = "http://example.invalid/feeds"',
            "PACKAGE_FEED_URIS",
        )

    def test_http_ni_main_rejected(self):
        self.check_rejected_feed(
            'NILRT_MACHINE_FEED_URI_nimain = "http://example.invalid/ni-main"',
            "NILRT_MACHINE_FEED_URI_nimain",
        )

    def test_http_additional_feed_rejected(self):
        self.check_rejected_feed(
            'PACKAGE_FEED_URIS_ADDITIONAL[NI-dist] = "http://example.invalid/dist"',
            "PACKAGE_FEED_URIS_ADDITIONAL[NI-dist]",
        )

    def test_http_build_feed_rejected(self):
        self.check_rejected_feed(
            'IPK_FEED_URIS = "external##http://example.invalid/feed"',
            "IPK_FEED_URIS[external]",
        )

    def test_empty_additional_feed_rejected(self):
        self.check_rejected_feed(
            'PACKAGE_FEED_URIS_ADDITIONAL[NI-dist] = ""',
            "PACKAGE_FEED_URIS_ADDITIONAL[NI-dist]",
        )

    def test_generated_opkg_configuration(self):
        from pathlib import Path
        from tempfile import TemporaryDirectory
        import bb
        import bb.build
        import bb.tinfoil

        self.write_config('NILRT_FEEDS_URI = "https://example.invalid/feeds"\n')
        fixtures = (
            ("# comment\nsrc/gz feed https://example.invalid/feed\n", True),
            ("src feed https://example.invalid/feed\n", True),
            ("src/gz feed http://example.invalid/feed\n", False),
            ("src/gz feed file:///tmp/feed\n", False),
            ("src/gz feed\n", False),
            ("option ssl_dont_verify_peer 0\n", False),
            ("option follow_location 0\n", False),
        )
        with bb.tinfoil.Tinfoil() as tinfoil:
            tinfoil.prepare()
            data = tinfoil.parse_recipe("nilrt-base-system-image")
            with TemporaryDirectory() as root:
                conf_dir = Path(root) / "etc" / "opkg"
                conf_dir.mkdir(parents=True)
                data.setVar("IMAGE_ROOTFS", root)
                data.setVar("T", str(Path(root) / "temp"))
                for contents, valid in fixtures:
                    with self.subTest(contents=contents):
                        (conf_dir / "test.conf").write_text(contents)
                        if valid:
                            bb.build.exec_func("nilrt_validate_https_feed_confs", data)
                        else:
                            with self.assertRaises(bb.BBHandledException):
                                bb.build.exec_func("nilrt_validate_https_feed_confs", data)
