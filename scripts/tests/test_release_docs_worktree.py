import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class ReleaseDocumentationTests(unittest.TestCase):
    def run_validation(self, workflow, fail=False):
        workflow_text = (ROOT / '.github/workflows' / workflow).read_text()
        self.assertIn('run: \"bash scripts/validate-release-source.sh\"', workflow_text)
        command = (ROOT / 'scripts/validate-release-source.sh').read_text()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            repo = root / 'repo'
            repo.mkdir()
            def git(*args):
                return subprocess.check_output(['git', *args], cwd=repo, text=True).strip()
            git('init', '-q')
            git('config', 'user.email', 'test@example.invalid')
            git('config', 'user.name', 'Test')
            (repo / 'sites').mkdir()
            (repo / 'sites/package.json').write_text('{}')
            reference = repo / 'reference.png'
            reference.write_text('original')
            git('add', '.')
            git('commit', '-qm', 'fixture')
            source = git('rev-parse', 'HEAD')
            tools = root / 'bin'
            tools.mkdir()
            npm = tools / 'npm'
            npm.write_text('#!/bin/sh\nset -eu\nprintf "%s\\n" "$*" >> "$CALL_LOG"\nif [ "$*" = "run screenshots:render" ]; then printf rendered >> ../reference.png; fi\nif [ "$*" = "run screenshots:check" ] && [ "$FAIL_CHECK" = "1" ]; then exit 42; fi\n')
            npm.chmod(0o755)
            env = dict(os.environ, PATH=str(tools) + os.pathsep + os.environ['PATH'], SOURCE_SHA=source, RUNNER_TEMP=str(root), CALL_LOG=str(root / 'calls'), FAIL_CHECK=str(int(fail)))
            cwd = repo
            result = subprocess.run(['bash', '-e', '-c', command], cwd=cwd, env=env, capture_output=True, text=True)
            self.assertEqual(result.returncode, 42 if fail else 0, result.stderr)
            self.assertEqual((root / 'calls').read_text().splitlines(), ['ci', 'run check', 'run screenshots:render', 'run screenshots:check'])
            self.assertEqual(reference.read_text(), 'original', 'Validation dirtied immutable release checkout')
            self.assertEqual(git('diff', '--name-only', source), '')
            self.assertEqual(git('worktree', 'list', '--porcelain').count('worktree '), 1)

    def test_jitpack_validation_preserves_release_source(self):
        self.run_validation('publish-jitpack.yml')

    def test_central_validation_preserves_release_source(self):
        self.run_validation('publish-library.yml')

    def test_failure_propagates_and_worktree_is_removed(self):
        for workflow in ('publish-jitpack.yml', 'publish-library.yml'):
            with self.subTest(workflow=workflow):
                self.run_validation(workflow, fail=True)
