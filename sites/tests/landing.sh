#!/bin/sh
set -eu
android-repo-setup build_docs --check
node --input-type=module <<'JS'
import {readFileSync} from 'node:fs';
import assert from 'node:assert/strict';
const rank=v=>v.includes('-')?(v.endsWith('-maven-central')?1:0):2;
const latest=(a,b)=>{
 const av=a.version.split('-')[0].split('.').map(Number),bv=b.version.split('-')[0].split('.').map(Number);
 for(let i=0;i<3;i++) if(av[i]!==bv[i]) return bv[i]-av[i];
 return rank(b.version)-rank(a.version);
};
assert.deepEqual(['0.1.9','0.1.10-jitpack','0.1.10-maven-central','0.1.10'].map(version=>({version})).sort(latest).map(s=>s.version),['0.1.10','0.1.10-maven-central','0.1.10-jitpack','0.1.9']);
const catalog=JSON.parse(readFileSync('sites/dist/versions.json','utf8'));
const scopes=Array.isArray(catalog)?catalog:catalog.scopes;
for(const language of ['en','es']) {
 const releases=scopes.filter(s=>s.module==='permission'&&s.language===language&&!s.development).sort(latest);
 assert.ok(releases.length);const html=readFileSync(`sites/dist/${language==='en'?'':language+'/'}index.html`,'utf8');assert.ok(html.includes(releases[0].root),`home must open ${releases[0].root}`);
 assert.ok(!html.includes('site-card'));
}
assert.ok(!scopes.some(s=>s.version==='0.2.1'),'legacy Central source must remain unknown');
JS
