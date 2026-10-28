import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { validateSite } from '../validate-docs.mjs';

test('checks HTML anchors and raw Markdown navigation, including reference links', async () => {
  const root = await mkdtemp(join(tmpdir(),'doc-links-'));
  try {
    await mkdir(join(root,'agents'));
    await writeFile(join(root,'index.html'), '<a href="/android.apexfission.permissions/agents/index.md#entry">Agent</a><a href="#missing">broken</a>');
    await writeFile(join(root,'agents/index.md'), '# Entry\n\n[Next][next]\n\n[next]: absent.md\n\n```text\n[ignored](absent-code.md)\n```');
    let errors = await validateSite(root);
    assert.equal(errors.length,2);
    assert.ok(errors.some(x => x.includes('#missing')));
    assert.ok(errors.some(x => x.includes('absent.md')));
    await writeFile(join(root,'index.html'), '<a href="/android.apexfission.permissions/agents/index.md#entry">Agent</a>');
    await writeFile(join(root,'agents/absent.md'), '# Present\n');
    assert.deepEqual(await validateSite(root),[]);
  } finally { await rm(root,{recursive:true,force:true}); }
});

test('validates version and language option routes and explicit Markdown anchors',async()=>{
 const root=await mkdtemp(join(tmpdir(),'doc-options-'));
 try{
  await mkdir(join(root,'es/permission/1.2.3'),{recursive:true});
  await writeFile(join(root,'index.html'),'<select><option value="/android.apexfission.permissions/es/permission/1.2.3/">Español</option></select><a href="guide.md#english-anchor">Raw</a>');
  await writeFile(join(root,'guide.md'),'# Guía\n\n<a id="english-anchor"></a>\n\n## Encabezado\n');
  assert.equal((await validateSite(root)).length,1);
  await writeFile(join(root,'es/permission/1.2.3/index.html'),'<h1>Versión</h1>');
  assert.deepEqual(await validateSite(root),[]);
 }finally{await rm(root,{recursive:true,force:true});}
});
