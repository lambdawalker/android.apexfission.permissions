import hashlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import urllib.error
import zipfile
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import module_release as m
import release_identity as identity
import import_docs
import documentation_history as history
import jitpack_build as build
import jitpack_release as jp


def record(version='0.2.2',source='a'*40,target='maven-central'):
    r=dict(module='permission',version=version,source=source,repository=target,repository_url=m.common.CENTRAL,group='com.apexfission.androi',artifact='permission',phase='confirmed-public',sha256={s:'b'*64 for s in m.common.SUFFIXES})
    if target=='jitpack':r.update(group=jp.GROUP,artifact=jp.ARTIFACT,consumer_version=f'permission~v{version}',repository_url=jp.REPOSITORY)
    return r

def zipped(entries):
    f=io.BytesIO()
    with zipfile.ZipFile(f,'w') as z:
        for n,v in entries.items():z.writestr(n,v)
    return f.getvalue()

class ReleaseTests(unittest.TestCase):
    def test_latest_numeric_and_destination_catchup(self):
        a=record('0.2.9');b=record('0.2.10',target='jitpack')
        rendered=import_docs.render({('maven-central','permission'):a,('jitpack','permission'):b},{'permission':('com.apexfission.androi','permission')})
        self.assertNotIn('### maven-central',rendered);self.assertIn('permission~v0.2.10',rendered)
        a=record('0.2.10');self.assertIn('### maven-central',import_docs.render({('maven-central','permission'):a,('jitpack','permission'):b},{'permission':('com.apexfission.androi','permission')}))
        b['source']='c'*40
        with self.assertRaisesRegex(ValueError,'Conflicting'):import_docs.render({('maven-central','permission'):a,('jitpack','permission'):b},{'permission':('com.apexfission.androi','permission')})
    def test_archive_retains_docs_identity_and_rejects_conflict(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d);path=history.archive_record(root,record());entry=json.loads((root/path).read_text());entry['documentation_ref']='d'*40;(root/path).write_text(json.dumps(entry))
            history.archive_record(root,record(target='jitpack'))
            entry=history.load_catalog(root)[0];self.assertEqual(entry['documentation_ref'],'d'*40);self.assertEqual(len(entry['destinations']),2)
            with self.assertRaisesRegex(ValueError,'source conflict'):history.archive_record(root,record(source='c'*40))
    def test_pending_destination_blocks_reupload_but_other_destination_allowed(self):
        tags=['release-pending/permission/0.2.2']
        with self.assertRaisesRegex(ValueError,'Unresolved'):m.ensure_available('permission',tags)
        with patch.dict(m.os.environ,{'RELEASE_REPOSITORY':'jitpack'}):m.ensure_available('permission',tags)
        with self.assertRaisesRegex(ValueError,'legacy'):m.ensure_available('permission',['release-pending/0.2.2'])
    def test_stable_jitpack_selection_and_task(self):
        for value in ('permission/v1.2.3','permission~v1.2.3'):self.assertEqual(build.selection(value),('permission','1.2.3'))
        for value in ('main','v1.2.3','permission/v01.2.3','permission/v1.2.3-SNAPSHOT','app/v1.2.3'):
            with self.assertRaises(ValueError):build.selection(value)
        self.assertEqual(build.command('permission','1.2.3')[2],':permission:publishToMavenLocal')
        with patch.dict(build.os.environ,{'VERSION':'permission~v1.2.3','GIT_COMMIT':'b'*40}),patch.object(build,'run',return_value='a'*40):
            with self.assertRaises(ValueError):build.main()
    def fixture(self):
        r=record(target='jitpack');v=r['consumer_version'];meta='<name>n</name><description>d</description><url>u</url><licenses><license><name>l</name><url>u</url></license></licenses><developers><developer><id>i</id><name>n</name></developer></developers><scm><url>u</url><connection>c</connection></scm>'
        pom=f'<project><groupId>{jp.GROUP}</groupId><artifactId>{jp.ARTIFACT}</artifactId><version>{v}</version><packaging>aar</packaging>{meta}<dependencies><dependency><groupId>com.google.accompanist</groupId><artifactId>accompanist-permissions</artifactId><version>0.37.3</version><scope>compile</scope></dependency></dependencies></project>'.encode()
        data={'.pom':pom,'.aar':zipped({'AndroidManifest.xml':'manifest','classes.jar':zipped({'com/apexfission/android/permission/Permission.class':b'\xca\xfe\xba\xbe\x00\x00\x00\x3d'})}),'-sources.jar':zipped({'Permission.kt':'class Permission'}),'-javadoc.jar':zipped({'docs/api.md':'API'})}
        r['content_sha256']={s:m.common.zip_contents(data[s]) for s in ('-sources.jar','-javadoc.jar')}
        files={jp.API_BASE+'/'+v:json.dumps({'status':'ok','commit':r['source']}).encode(),**{jp.artifact_base(r)+s:d for s,d in data.items()}}
        return r,files
    def test_jitpack_provenance_content_partial_and_retries(self):
        r,files=self.fixture();read=lambda url,missing=False:files.get(url)
        self.assertEqual(len(jp.verify(r,read)),4)
        api=jp.API_BASE+'/'+r['consumer_version']
        for status in ('queued','building','none'):
            files[api]=json.dumps({'status':status}).encode()
            with self.assertRaises(jp.NotReady):jp.verify(r,read)
        files[api]=json.dumps({'status':'ok','commit':'b'*40}).encode()
        with self.assertRaisesRegex(ValueError,'source'):jp.verify(r,read)
        files[api]=json.dumps({'status':'ok','commit':r['source']}).encode()
        base=jp.artifact_base(r);files[base+'-sources.jar']=zipped({'Permission.kt':'changed source'})
        with self.assertRaisesRegex(ValueError,'content differs'):jp.verify(r,read)
        del files[base+'-sources.jar']
        with self.assertRaises(jp.NotReady):jp.verify(r,read)
    def test_timeout_keeps_journal_and_docs(self):
        r=record();r['phase']='reserved-before-upload'
        with patch.object(m,'read_record',return_value=r),patch.object(m,'git',return_value=r['source']),patch.object(m,'verify_public',side_effect=urllib.error.URLError('missing')):
            with self.assertRaises(TimeoutError):m.confirm('permission','0.2.2',timeout=0)
    def test_registry_and_generated_docs(self):
        from publishing_config import generate
        generate(check=True);m.documentation(verify=True);history.verify(m.ROOT)

class GitTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup);self.root=Path(self.temp.name)
        self.git('init','-q');self.git('config','user.email','test@example.com');self.git('config','user.name','test')
        self.write('gradle.properties','GROUP=com.apexfission.androi\nPOM_ARTIFACT_ID=permission\n');self.write('permission/src/main/code.kt','one');self.commit();self.source=self.git('rev-parse','HEAD');self.git('tag','permission/v0.2.9')
    def git(self,*args):return subprocess.check_output(['git',*args],cwd=self.root,text=True,stderr=subprocess.DEVNULL).strip()
    def write(self,path,value):
        p=self.root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(value)
    def commit(self):self.git('add','.');self.git('commit','-qm','change')
    def select(self,requested=''):return identity.select('permission',self.git('rev-parse','HEAD'),self.git,self.git('tag').splitlines(),[],requested)
    def test_docs_only_unchanged_reuses_original_and_changed_input_advances(self):
        self.write('IMPORT.md','updated');self.write('docs/es/index.md','translation');self.commit();self.assertEqual(self.select(),('0.2.9',self.source))
        self.write('permission/src/main/code.kt','two');self.commit();self.assertEqual(self.select()[0],'0.2.10')
    def test_pending_cross_destination_and_legacy_conflicts(self):
        self.git('tag','release-pending/jitpack/permission/0.2.10');self.assertEqual(self.select(),('0.2.10',self.source))
        self.write('gradle/libs.versions.toml','change');self.commit();self.git('tag','v0.2.10')
        with self.assertRaisesRegex(ValueError,'Conflicting'):self.select()
    def test_major_override_and_older_rejection(self):
        self.assertEqual(self.select('1.0.0'),('1.0.0',self.source))
        with self.assertRaises(ValueError):self.select('0.2.8')


class FinalizationTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup);self.base=Path(self.temp.name);self.remote=self.base/'remote.git';self.root=self.base/'repo'
        self.call(self.base,'init','--bare','--initial-branch=main',str(self.remote));self.call(self.base,'clone',str(self.remote),str(self.root));self.git('config','user.email','test@example.com');self.git('config','user.name','test')
        for path in ('publishing/legacy-installation.json','docs/templates/IMPORT.md.template','gradle.properties'):
            destination=self.root/path;destination.parent.mkdir(parents=True,exist_ok=True);destination.write_bytes((m.ROOT/path).read_bytes())
        (self.root/'IMPORT.md').write_text('previous installation');self.git('add','.');self.git('commit','-qm','release source');self.source=self.git('rev-parse','HEAD');self.git('push','origin','main')
        patcher=patch.object(m,'ROOT',self.root);patcher.start();self.addCleanup(patcher.stop)
    def call(self,cwd,*args):return subprocess.check_output(['git',*args],cwd=cwd,text=True,stderr=subprocess.PIPE).strip()
    def git(self,*args):return self.call(self.root,*args)
    def reserve_fixture(self):
        r=record(source=self.source);r['phase']='reserved-before-upload';self.git('tag','-a',m.pending('permission','0.2.2'),self.source,'-m',json.dumps(r));self.git('tag','permission/v0.2.2',self.source);self.git('push','origin','--tags');return r
    def test_guard_is_durable_and_second_invocation_rejected(self):
        self.reserve_fixture();m.guard('permission','0.2.2')
        with self.assertRaisesRegex(ValueError,'already started'):m.guard('permission','0.2.2')
        self.assertTrue(m.remote_ref(m.uploading('permission','0.2.2')))
    def test_finalization_preserves_advanced_main_and_retry_noops(self):
        r=self.reserve_fixture();r['phase']='confirmed-public';(self.root/'build').mkdir();(self.root/'build/confirmed-permission.json').write_text(json.dumps(r))
        (self.root/'unrelated.md').write_text('concurrent change');self.git('add','unrelated.md');self.git('commit','-qm','concurrent update');self.git('push','origin','main');self.git('checkout','--detach',self.source)
        m.finalize('permission','0.2.2',self.source)
        self.assertEqual(self.call(self.remote,'show','main:unrelated.md'),'concurrent change')
        archive=json.loads(self.call(self.remote,'show','main:docs/releases/history/permission/0.2.2.json'));self.assertEqual(archive['source'],self.source)
        self.assertFalse(m.remote_ref(m.pending('permission','0.2.2')))
        self.assertEqual(m.prepare_finalization('permission','0.2.2',self.source)['skip'],'true')
    def test_refused_atomic_push_retains_remote_journal_and_docs(self):
        r=self.reserve_fixture();r['phase']='confirmed-public';(self.root/'build').mkdir();(self.root/'build/confirmed-permission.json').write_text(json.dumps(r))
        hook=self.remote/'hooks/pre-receive';hook.write_text('#!/bin/sh\nexit 1\n');hook.chmod(0o755)
        with self.assertRaises(subprocess.CalledProcessError):m.finalize('permission','0.2.2',self.source)
        self.assertTrue(m.remote_ref(m.pending('permission','0.2.2')))
        self.assertEqual(self.call(self.remote,'show','main:IMPORT.md'),'previous installation')

    def test_unproven_legacy_floor_applies_before_first_jitpack(self):
        with patch.dict(m.os.environ,{'RELEASE_REPOSITORY':'jitpack'}):
            self.assertEqual(m.prepare('permission')['version'],'0.2.2')
            with self.assertRaisesRegex(ValueError,'legacy'):m.prepare('permission','0.2.1')


class NegativeValidationTests(unittest.TestCase):
    def test_pom_and_aar_identity_and_shape(self):
        fixture=ReleaseTests();r,files=fixture.fixture();base=jp.artifact_base(r);pom=files[base+'.pom']
        for replacement in (pom.replace(jp.GROUP.encode(),b'wrong.group'),pom.replace(r['consumer_version'].encode(),b'9.9.9'),pom.replace(b'<packaging>aar',b'<packaging>jar')):
            with self.assertRaises(ValueError):m.common.verify_pom(replacement,r['group'],r['artifact'],r['consumer_version'])
        with self.assertRaises(ValueError):m.common.verify_artifact(zipped({'AndroidManifest.xml':'only manifest'}),'.aar',r['group'],r['artifact'],r['consumer_version'])
        bad=zipped({'AndroidManifest.xml':'manifest','classes.jar':zipped({'com/apexfission/android/permission/Test.class':b'bad class'})})
        with self.assertRaises(ValueError):m.common.verify_artifact(bad,'.aar',r['group'],r['artifact'],r['consumer_version'])
    def test_registry_rejects_duplicates_unsupported_publishers_and_unsafe_urls(self):
        import publishing_config as config
        with tempfile.TemporaryDirectory() as d:
            path=Path(d)/'registry.yml'
            for value in ('repositories:\n  maven-central: {environment: central, publisher: central}\n  maven-central: {environment: second, publisher: central}\n', 'repositories:\n  maven-central: {environment: central, publisher: central}\n  private: {environment: private, publisher: maven}\n'):
                path.write_text(value)
                with self.assertRaises(ValueError):config.load_config(path)
        for value in ('http://example.org','https://user:password@example.org','https://example.org/\"code','https://example.org/?secret=yes'):
            with self.assertRaises(ValueError):config.validate_url(value)

if __name__=='__main__':unittest.main()
