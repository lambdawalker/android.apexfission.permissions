import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile, readFile, cp, symlink, rm, access } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { execFileSync } from 'node:child_process';

// Exercise the publisher in an isolated repository, including stale build outputs.
test('raw navigation stays Markdown, nested files publish, stale outputs disappear, code is untouched', async () => {
  const root = await mkdtemp(join(tmpdir(), 'permission-docs-'));
  try {
    for (const p of ['sites/scripts', 'sites/src/content/docs', 'sites/public/agents', 'sites/public/screenshots', 'docs/agents/nested', 'docs/screenshots']) await mkdir(join(root,p), {recursive:true});
    await cp(resolve('scripts'), join(root,'sites/scripts'), {recursive:true});
    await symlink(resolve('node_modules'), join(root,'sites/node_modules'));
    for (const file of ['api','concepts','limitations','troubleshooting','migration','recipes','quickstart']) await writeFile(join(root,`docs/agents/${file}.md`), '# Guide\n');
    await writeFile(join(root,'IMPORT.md'), '# Install\n');
    await writeFile(join(root,'docs/agents/index.md'), '# Entry\n\n[Next](nested/next.md#details)\n\n[Install](../../IMPORT.md)\n\n[Source](../../app/Foo.kt)\n\n[Reference][next]\n\n[next]: nested/next.md#details\n\n```text\n[Literal](not-a-link.md)\n```\n');
    await writeFile(join(root,'docs/agents/nested/next.md'), '# Next\n\n## Details\n');
    await writeFile(join(root,'sites/public/agents/stale.md'), 'old');
    await writeFile(join(root,'sites/public/screenshots/stale.png'), 'old');
    execFileSync(process.execPath, ['scripts/sync-screenshots.mjs'], {cwd:join(root,'sites')});
    const result = await readFile(join(root,'sites/public/agents/index.md'),'utf8');
    assert.match(result, /\]\(nested\/next\.md#details\)/);
    assert.match(result, /\]\(\.\.\/IMPORT\.md\)/);
    assert.match(result, /github\.com\/lambdawalker\/android.apexfission.permissions\/blob\/main\/app\/Foo.kt/);
    assert.match(result, /\[next\]: nested\/next\.md#details/);
    assert.match(result, /\[Literal\]\(not-a-link\.md\)/);
    await access(join(root,'sites/public/agents/nested/next.md'));
    await assert.rejects(access(join(root,'sites/public/agents/stale.md')));
    await assert.rejects(access(join(root,'sites/public/screenshots/stale.png')));
  } finally { await rm(root,{recursive:true,force:true}); }
});
