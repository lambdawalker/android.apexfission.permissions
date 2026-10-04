import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location('release', Path(__file__).parents[1] / 'release.py')
release = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(release)


class ReleaseTests(unittest.TestCase):
    def test_numeric_patch_order_and_ignore_nonrelease_tags(self):
        self.assertEqual(release.next_version(['v0.2.9', 'v0.2.10', 'v2.0.0-rc1'], ['0.2.10']), '0.2.11')

    def test_bootstrap_from_published_versions(self):
        self.assertEqual(release.next_version([], ['0.1.0', '0.2.1']), '0.2.2')

    def test_central_ahead_of_tags_requires_reconciliation(self):
        with self.assertRaisesRegex(ValueError, 'ahead'):
            release.next_version(['v0.2.0'], ['0.2.0', '0.2.1'])

    def test_tags_without_confirmed_publication_are_rejected(self):
        with self.assertRaisesRegex(ValueError, 'not published'):
            release.next_version(['v0.3.0'], ['0.2.1'])

    def test_no_history_does_not_guess_a_version(self):
        with self.assertRaises(ValueError):
            release.next_version([], [])

    def test_render_and_version_marker(self):
        template = '<!-- release-version: {{VERSION}} -->\n{{GROUP_ID}}:{{ARTIFACT_ID}}:{{VERSION}}\n'
        result = release.render(template, 'org.example', 'lib', '1.2.3')
        self.assertEqual(result, '<!-- release-version: 1.2.3 -->\norg.example:lib:1.2.3\n')
        self.assertEqual(release.document_version(result), '1.2.3')

    def test_reject_invalid_version_and_unknown_placeholder(self):
        for version in ['01.2.3', '1.2.3-SNAPSHOT', '$(echo unsafe)', '1.2']:
            with self.subTest(version=version), self.assertRaises(ValueError):
                release.render('{{VERSION}}', 'g', 'a', version)
        with self.assertRaisesRegex(ValueError, 'placeholder'):
            release.render('{{TYPO}}', 'g', 'a', '1.2.3')

    def test_pom_must_match_all_coordinates(self):
        pom = b'<project><groupId>g</groupId><artifactId>a</artifactId><version>1.2.3</version></project>'
        release.verify_pom(pom, 'g', 'a', '1.2.3')
        with self.assertRaises(ValueError):
            release.verify_pom(pom, 'g', 'a', '1.2.4')

    def test_regeneration_preserves_release_and_detects_template_or_coordinate_drift(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / 'docs/templates').mkdir(parents=True)
            template = root / 'docs/templates/IMPORT.md.template'
            template.write_text('<!-- release-version: {{VERSION}} -->\n{{GROUP_ID}}:{{ARTIFACT_ID}}\n')
            (root / 'gradle.properties').write_text('GROUP=example\nPOM_ARTIFACT_ID=library\n')
            output = root / 'IMPORT.md'
            output.write_text('<!-- release-version: 1.2.3 -->\nold text\n')
            with patch.object(release, 'ROOT', root):
                with patch.object(sys, 'argv', ['release.py', 'generate']):
                    release.main()
                self.assertEqual(output.read_text(), '<!-- release-version: 1.2.3 -->\nexample:library\n')
                with patch.object(sys, 'argv', ['release.py', 'verify']):
                    release.main()
                    template.write_text(template.read_text() + 'new content\n')
                    with self.assertRaisesRegex(ValueError, 'stale'):
                        release.main()
                with patch.object(sys, 'argv', ['release.py', 'generate']):
                    release.main()
                (root / 'gradle.properties').write_text('GROUP=changed\nPOM_ARTIFACT_ID=library\n')
                with patch.object(sys, 'argv', ['release.py', 'verify']):
                    with self.assertRaisesRegex(ValueError, 'stale'):
                        release.main()


if __name__ == '__main__':
    unittest.main()
