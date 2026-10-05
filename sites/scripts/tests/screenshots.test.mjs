import test from 'node:test';
import assert from 'node:assert/strict';
import sharp from 'sharp';
import { samePixels, findCapture, inputHash } from '../screenshots.mjs';

test('pixel comparison ignores PNG encoding but detects changed pixels', async () => {
 const image = {create:{width:2,height:2,channels:4,background:'#123456'}};
 const a = await sharp(image).png({compressionLevel:0}).toBuffer();
 const b = await sharp(image).png({compressionLevel:9}).toBuffer();
 const c = await sharp({create:{width:2,height:2,channels:4,background:'#654321'}}).png().toBuffer();
 assert.equal(await samePixels(a,b),true);
 assert.equal(await samePixels(a,c),false);
});
test('capture lookup rejects absent or ambiguous method matches', () => {
 const paths = ['pkg/PermissionBundleScreenshotTest.firstPage_0.png'];
 assert.equal(findCapture('firstPage',paths),paths[0]);
 assert.throws(()=>findCapture('first',paths),/exactly one/);
 assert.throws(()=>findCapture('firstPage',[...paths,'pkg/firstPage_1.png']),/exactly one/);
});

import { mkdtemp, mkdir, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { execFileSync } from 'node:child_process';

test('capture input hash includes new untracked source files', async () => {
 const root=await mkdtemp(join(tmpdir(),'capture-inputs-'));
 try {
  execFileSync('git',['init','-q'],{cwd:root});
  await mkdir(join(root,'permission/src/main'),{recursive:true});
  const file=join(root,'permission/src/main/New.kt');
  const before=await inputHash(root);
  await writeFile(file,'new source');
  const added=await inputHash(root);
  assert.notEqual(added,before);
  await writeFile(file,'changed source');
  assert.notEqual(await inputHash(root),added);
 } finally { await rm(root,{recursive:true,force:true}); }
});
