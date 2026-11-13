import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync, readdirSync, statSync} from 'node:fs';
import {resolve, relative} from 'node:path';
import {equivalentPeers, versionOptions, publications} from '../astro/components/ReleasePresentation.mjs';
const root = resolve(import.meta.dirname, '../..');
const dist = resolve(root, 'sites/dist');
const read = path => readFileSync(resolve(dist, path), 'utf8');
const scopes = JSON.parse(read('versions.json'));
const publicRoot = resolve(root, 'sites/static');
const evidencePath = resolve(publicRoot, 'release-equivalence.json');
const prefix = new URL(readFileSync(resolve(root, '.project/docs/config.yaml'),'utf8').match(/base_url: (.*)/)[1]).pathname.replace(/\/$/,'');
const html = href => read(href.slice(prefix.length).replace(/^\//,'') + 'index.html');
const options = page => [...page.matchAll(/<select[^>]*aria-label="(?:Version|Versión)"[^>]*>([\s\S]*?)<\/select>/g)][0]?.[1] || '';
const destination = (scope, guide) => scope.pages.find(page=>page.slug===guide)?.href || scope.root;
const walk = path => readdirSync(path).flatMap(name => {const file=resolve(path,name); return statSync(file).isDirectory()?walk(file):[file];});
test('Astro latest release entry, exact versions, separate Development and scoped search',()=>{
 const released=scopes.filter(scope=>scope.language==='en'&&!scope.development).sort((a,b)=>{
  const av=a.version.split('-')[0].split('.').map(Number),bv=b.version.split('-')[0].split('.').map(Number);
  for(let i=0;i<3;i++)if(av[i]!==bv[i])return av[i]-bv[i];
  return Number(a.version.endsWith('-maven-central'))-Number(b.version.endsWith('-maven-central'));
 });
 const latest=released.at(-1);
 assert.ok(read('index.html').includes(`url=${latest.root}`));
 for(const scope of scopes){
  const page=html(scope.root), select=options(page);
  assert.ok(select.includes('Development')||select.includes('Desarrollo'));
  assert.ok(page.includes(`version:${scope.version}`));
  assert.ok(page.includes('data-filters=')&&page.includes('data-doc-navigation'));
  assert.ok(page.includes('Site build revision')||page.includes('Revisión de compilación'));
  for(const image of scope.images || []) assert.ok(readFileSync(resolve(dist,image.src.slice(prefix.length).replace(/^\//,''))).length>0);
  for(const guide of scope.pages){assert.ok(read(guide.raw.slice(prefix.length).replace(/^\//,'')).length>0);}
  const guide=scope.pages.find(page=>page.slug&&page.slug!=='installation');
  if(guide){const output=html(guide.href);for(const match of options(output).matchAll(/<option value="([^"]+)"/g)){const target=scopes.find(peer=>match[1].startsWith(peer.root));assert.ok(target);assert.equal(target.language,scope.language);assert.equal(target.module,scope.module);if(target.pages.some(page=>page.slug===guide.slug)){assert.equal(match[1],destination(target,guide.slug));}else{const fallback=new URL(match[1],'https://fixture.invalid');assert.equal(fallback.pathname,destination(target,''));assert.equal(fallback.searchParams.get('guide-unavailable'),guide.slug);assert.equal(fallback.hash,'#guide-unavailable');}}const other=scopes.find(peer=>peer.language!==scope.language&&peer.version===scope.version);if(other?.pages.some(page=>page.slug===guide.slug))assert.ok(output.includes(destination(other,guide.slug)));}
 }
 for(const endpoint of ['IMPORT.md','llms.txt','agents/index.md'])assert.ok(read(endpoint).length>0);
});
test('all retained static aliases and screenshot URLs survive byte-for-byte',()=>{
 for(const source of walk(resolve(root,'sites/static'))){const path=relative(resolve(root,'sites/static'),source);assert.deepEqual(readFileSync(resolve(dist,path)),readFileSync(source),path);}
});
test('same version text never establishes provider equivalence',()=>{
 const sources={'maven-central':'a'.repeat(40),jitpack:'b'.repeat(40)};
 const pair=Object.entries(sources).map(([name,source])=>({module:'demo-common',language:'en',version:`0.1.2-${name}`,source,root:`/${name}/`,pages:[]}));
 const evidence=[{module:'demo-common',version:'0.1.2',sources,sha256:Object.fromEntries(['.aar','-sources.jar','-javadoc.jar'].map(key=>[key,'c'.repeat(64)]))}];
 assert.equal(versionOptions(pair,pair[0],'',destination,[]).length,2);
 assert.equal(publications(pair,pair[0],[]).length,1);
 assert.equal(equivalentPeers(pair,pair[0],evidence).length,2);
 assert.equal(versionOptions(pair,pair[1],'',destination,evidence).length,1);
 assert.ok(versionOptions(pair,pair[1],'',destination,evidence)[0].selected);
 assert.equal(publications(pair,pair[0],evidence).length,2);
 assert.equal(equivalentPeers([pair[0],{...pair[1],source:'unverified'}],pair[0],evidence).length,1);
 assert.equal(equivalentPeers(pair,pair[0],[{...evidence[0],sha256:{}}]).length,1);
});

test('release scopes keep exact confirmed history without inventing a legacy 0.2.1 manual',()=>{
 assert.ok(!scopes.some(scope=>scope.version.startsWith('0.2.1')));
 for(const scope of scopes.filter(scope=>!scope.development)){
  const record=JSON.parse(readFileSync(resolve(root,`docs/releases/history/permission/${scope.version}.json`),'utf8'));
  assert.equal(scope.source,record.source);
  assert.equal(scope.documentation_ref,record.documentation_ref || record.source);
  assert.deepEqual([...scope.destinations].sort(),Object.keys(record.destinations).sort());
  const page=html(scope.root);
  for(const provider of publications(scopes,scope))assert.ok(page.includes(provider.href));
 }
});
