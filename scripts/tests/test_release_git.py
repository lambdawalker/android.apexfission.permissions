"""Exercise release recovery and atomic finalization against real temporary Git repos."""
import contextlib
import io
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

from test_release import release

FINALIZE = Path(__file__).parents[1] / 'finalize-release.sh'


class GitReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.remote = self.base / 'remote.git'
        self.repo = self.base / 'repo'
        self.run_git(self.base, 'init', '--bare', '--initial-branch=main', str(self.remote))
        self.run_git(self.base, 'clone', str(self.remote), str(self.repo))
        self.git('config', 'user.name', 'Test')
        self.git('config', 'user.email', 'test@example.invalid')
        (self.repo / 'IMPORT.md').write_text('old published version\n')
        (self.repo / 'gradle.properties').write_text('GROUP=example\nPOM_ARTIFACT_ID=lib\n')
        self.git('add', '.')
        self.git('commit', '-m', 'artifact source')
        self.source = self.git('rev-parse', 'HEAD')
        self.git('tag', 'release-pending/1.2.3')
        self.git('push', 'origin', 'main', '--tags')
        self.git('checkout', '--detach')
        (self.repo / 'IMPORT.md').write_text('new confirmed version\n')

    @staticmethod
    def run_git(cwd, *args):
        return subprocess.check_output(['git', *args], cwd=cwd, text=True, stderr=subprocess.PIPE).strip()

    def git(self, *args):
        return self.run_git(self.repo, *args)

    def remote_git(self, *args):
        return self.run_git(self.remote, *args)

    def finalize(self):
        return subprocess.run(['bash', str(FINALIZE)], cwd=self.repo, capture_output=True, text=True,
                              env={**os.environ, 'SOURCE_SHA': self.source, 'RELEASE_VERSION': '1.2.3'})

    def test_tag_points_to_artifact_source_and_docs_commit_is_later(self):
        result = self.finalize()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.remote_git('rev-parse', 'v1.2.3^{commit}'), self.source)
        self.assertNotEqual(self.remote_git('rev-parse', 'main'), self.source)
        self.assertEqual(self.remote_git('show', 'main:IMPORT.md'), 'new confirmed version')
        self.assertEqual(self.remote_git('tag', '--list', 'release-pending/*'), '')

    def test_failed_push_does_not_partially_update_remote(self):
        hook = self.remote / 'hooks/pre-receive'
        hook.write_text('#!/bin/sh\necho "simulated branch protection" >&2\nexit 1\n')
        hook.chmod(0o755)
        result = self.finalize()
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(self.remote_git('rev-parse', 'main'), self.source)
        self.assertEqual(self.remote_git('show', 'main:IMPORT.md'), 'old published version')
        self.assertEqual(self.remote_git('tag', '--list', 'v*'), '')
        self.assertEqual(self.remote_git('tag', '--list', 'release-pending/*'), 'release-pending/1.2.3')

    def advance_main(self, path):
        # Simulate another contributor landing work while Maven publishes.
        self.git('restore', 'IMPORT.md')
        self.git('switch', 'main')
        (self.repo / path).write_text('concurrent edit\n')
        self.git('add', path)
        self.git('commit', '-m', 'concurrent change')
        self.git('push', 'origin', 'main')
        self.git('checkout', '--detach', self.source)
        (self.repo / 'IMPORT.md').write_text('new confirmed version\n')

    def test_unrelated_main_change_is_preserved(self):
        self.advance_main('README.md')
        result = self.finalize()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.remote_git('show', 'main:README.md'), 'concurrent edit')
        self.assertEqual(self.remote_git('rev-parse', 'v1.2.3^{commit}'), self.source)

    def test_coordinate_change_stops_finalization(self):
        self.advance_main('gradle.properties')
        result = self.finalize()
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(self.remote_git('show', 'main:IMPORT.md'), 'old published version')
        self.assertEqual(self.remote_git('tag', '--list', 'v*'), '')

    def test_pending_attempt_blocks_new_publication(self):
        with patch.object(release, 'ROOT', self.repo):
            with self.assertRaisesRegex(ValueError, 'Unresolved release attempt'):
                release.prepare('')

    def test_recovery_uses_original_source(self):
        output = self.base / 'output'
        with patch.object(release, 'ROOT', self.repo), patch.dict(os.environ, GITHUB_OUTPUT=str(output)):
            with contextlib.redirect_stdout(io.StringIO()):
                release.prepare('1.2.3')
        self.assertEqual(output.read_text(), f'version=1.2.3\nsource={self.source}\n')

    def test_recovery_requires_exact_pending_version(self):
        with patch.object(release, 'ROOT', self.repo):
            with self.assertRaisesRegex(ValueError, 'matching pending'):
                release.prepare('1.2.4')


if __name__ == '__main__':
    unittest.main()
