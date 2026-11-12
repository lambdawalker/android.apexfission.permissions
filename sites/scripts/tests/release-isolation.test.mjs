import test from 'node:test';
import assert from 'node:assert/strict';
import {mkdtempSync,mkdirSync,writeFileSync,readFileSync,rmSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {execFileSync,spawnSync} from 'node:child_process';
const script = readFileSync(new URL('../../../scripts/validate-release-source.sh',import.meta.url),'utf8');
for (const fail of [false,true]) test(`release validation isolates source and cleans up (failure=${fail})`,()=>{
 const dir=mkdtempSync(join(tmpdir(),'permission-release-'));const repo=join(dir,'repo');mkdirSync(repo); const bin=join(dir,'bin');mkdirSync(bin);
 const git=(...args)=>execFileSync('git',args,{cwd:repo,encoding:'utf8'}).trim();
 try {
 git('init','-q');git('config','user.email','test@example.invalid');git('config','user.name','Test');writeFileSync(join(repo,'reference.png'),'original');git('add','.');git('commit','-qm','fixture');const source=git('rev-parse','HEAD');
 for(const tool of ['node','npm','android-repo-setup']) writeFileSync(join(bin,tool),'#!/bin/sh\nset -eu\nprintf "%s %s\\n" "'+tool+'" "$*" >> "$CALL_LOG"\ncase "$*" in *screenshots:render*) printf rendered >> reference.png;; *screenshots:check*) if [ "$FAIL_CHECK" = 1 ]; then exit 42; fi;; esac\n',{mode:0o755});
 const result=spawnSync('bash',['-e','-c',script],{cwd:repo,env:{...process.env,PATH:bin+':'+process.env.PATH,SOURCE_SHA:source,RUNNER_TEMP:dir,CALL_LOG:join(dir,'calls'),FAIL_CHECK:fail?'1':'0'},encoding:'utf8'});
 assert.equal(result.status,fail?42:0,result.stderr);assert.equal(readFileSync(join(repo,'reference.png'),'utf8'),'original');assert.equal(git('status','--porcelain'),'');assert.equal(git('worktree','list','--porcelain').match(/^worktree /gm).length,1);
 assert.deepEqual(readFileSync(join(dir,'calls'),'utf8').trim().split('\n'),['node sites/scripts/sync-examples.mjs','node sites/scripts/screenshots.mjs manifest','android-repo-setup build_docs --check','npm --prefix sites ci','npm --prefix sites run screenshots:render','npm --prefix sites run screenshots:check']);
 } finally {rmSync(dir,{recursive:true,force:true});}
});
