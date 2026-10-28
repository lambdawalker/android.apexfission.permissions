import { readFile, writeFile, readdir, mkdir, cp, rm } from 'node:fs/promises';
import { resolve, relative, dirname } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import sharp from 'sharp';
const root = fileURLToPath(new URL('../../',import.meta.url));
const manifestPath = resolve(root,'docs/screenshots/manifest.json');
const digest = bytes => createHash('sha256').update(bytes).digest('hex');
async function walk(dir) {
 const result=[];
 for (const entry of await readdir(dir,{withFileTypes:true})) {
  const path=resolve(dir,entry.name);
  if(entry.isDirectory()) result.push(...await walk(path)); else result.push(path);
 }
 return result.sort();
}
export function findCapture(method, files) {
 if(!/^[A-Za-z][A-Za-z0-9]*$/.test(method)) throw new Error('Invalid preview method');
 const pattern=new RegExp(`(?:^|[/.])${method}(?:[_. /]|$)`);
 const matches=files.filter(path=>path.endsWith('.png') && pattern.test(path));
 if(matches.length!==1) throw new Error(`Expected exactly one capture for ${method}; found ${matches.length}`);
 return matches[0];
}
export async function samePixels(a,b) {
 const [x,y]=await Promise.all([a,b].map(buffer=>sharp(buffer).ensureAlpha().raw().toBuffer({resolveWithObject:true})));
 return x.info.width===y.info.width && x.info.height===y.info.height && x.data.equals(y.data);
}
export async function inputHash(directory = root) {
 const names = execFileSync('git',['ls-files','--cached','--others','--exclude-standard','permission/src/main','permission/src/screenshotTest','permission/build.gradle.kts','build.gradle.kts','gradle','gradle.properties','settings.gradle.kts'],{cwd:directory,encoding:'utf8'}).trim().split('\n').filter(Boolean).sort();
 const hash=createHash('sha256');
 for(const name of new Set(names)) {
  hash.update(name+'\0');
  try { hash.update(await readFile(resolve(directory,name))); } catch(error) { if(error.code!=='ENOENT') throw error; hash.update('<deleted>'); }
 }
 return hash.digest('hex');
}
async function main() {
 const mode=process.argv[2]??'manifest';
 if(!['manifest','render','check','update'].includes(mode)) throw new Error('Use manifest, render, check, or update [render-directory]');
 const manifest=JSON.parse(await readFile(manifestPath,'utf8'));
 const names=new Set();
 for(const scenario of manifest.scenarios) {
  if(!/^[a-z0-9-]+\.png$/.test(scenario.file)||names.has(scenario.file)) throw new Error('Invalid/duplicate screenshot filename');
  names.add(scenario.file);
  const bytes=await readFile(resolve(root,'docs/screenshots',scenario.file));
  if(digest(bytes)!==scenario.sha256) throw new Error(`Unrecorded image change: ${scenario.file}. Use the intentional update command.`);
  const metadata=await sharp(bytes).metadata();
  if(metadata.format!=='png'||!scenario.alt||!scenario.preview||!scenario.capture_kind) throw new Error(`Incomplete scenario: ${scenario.file}`);
 }
 if(mode==='manifest') { console.log(`Validated ${names.size} reviewed screenshot assets (not a fresh render).`); return; }
 const renderDir=resolve(root,process.argv[3]??'permission/src/screenshotTestDebug/reference');
 const capturePath=resolve(renderDir,'capture.json');
 if(mode==='render') {
  if(process.argv[3]) throw new Error('render uses the Gradle reference output directory');
  const before=await inputHash();
  await rm(renderDir,{recursive:true,force:true});
  execFileSync(resolve(root,'gradlew'),[':permission:updateDebugScreenshotTest','--stacktrace'],{cwd:root,stdio:'inherit'});
  if(before!==await inputHash()) throw new Error('Screenshot inputs changed while rendering');
  const capture={source_commit:execFileSync('git',['rev-parse','HEAD'],{cwd:root,encoding:'utf8'}).trim(),input_sha256:before,working_tree_dirty:!!execFileSync('git',['status','--porcelain'],{cwd:root,encoding:'utf8'}).trim(),platform:process.platform,architecture:process.arch};
  await writeFile(capturePath,JSON.stringify(capture,null,2)+'\n');
  console.log('Candidates rendered. Run screenshots:check; review before screenshots:update.');
  return;
 }
 const capture=JSON.parse(await readFile(capturePath,'utf8'));
 if(capture.input_sha256!==await inputHash() || !/^[a-f0-9]{40}$/.test(capture.source_commit)) throw new Error('Capture provenance is missing or does not match current inputs; render again.');
 const files=await walk(renderDir);
 // Resolve every scenario before modifying accepted assets.
 const captures=manifest.scenarios.map(s=>findCapture(s.preview,files));
 const errors=[];
 for(const [i,scenario] of manifest.scenarios.entries()) {
  const target=resolve(root,'docs/screenshots',scenario.file);
  const bytes=await readFile(captures[i]);
  if(mode==='check') {
   if(!await samePixels(await readFile(target),bytes)) errors.push(scenario.file);
  } else {
   await cp(captures[i],target);
   scenario.sha256=digest(bytes);
   scenario.source_commit=capture.source_commit;
   scenario.capture_environment=capture;
   scenario.input_sha256=await inputHash();
   scenario.provenance='Locally rendered candidates accepted with screenshots:update; review images before committing.';
  }
 }
 if(mode==='update') await writeFile(manifestPath,JSON.stringify(manifest,null,2)+'\n');
 const evidence={source_commit:execFileSync('git',['rev-parse','HEAD'],{cwd:root,encoding:'utf8'}).trim(),input_sha256:await inputHash(),mode,compared:manifest.scenarios.map(s=>s.file),differences:errors};
 const output=resolve(root,'build/documentation-screenshots/evidence.json');
 await mkdir(dirname(output),{recursive:true}); await writeFile(output,JSON.stringify(evidence,null,2)+'\n');
 if(errors.length) throw new Error(`Documentation screenshot changes: ${errors.join(', ')}. Review candidates; never accept automatically in CI.`);
 console.log(`${mode}: ${manifest.scenarios.length} screenshots passed. Evidence: ${relative(root,output)}`);
}
if(process.argv[1] && import.meta.url===pathToFileURL(resolve(process.argv[1])).href) await main();
