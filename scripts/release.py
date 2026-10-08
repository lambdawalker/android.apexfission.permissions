"""Signed Android release policy, durable attempt journal, and deterministic docs.

Standard library only. Network failures are never treated as an empty registry.
"""
import argparse
import hashlib
import io
import json
import os
from pathlib import Path
import re
import subprocess
import time
import tomllib
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
CENTRAL = 'https://repo.maven.apache.org/maven2'
SEMVER = r'(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)'
SUFFIXES = ('.pom', '.aar', '-sources.jar', '-javadoc.jar', '.module')


def version_key(value):
    if not re.fullmatch(SEMVER, value):
        raise ValueError(f'Expected stable X.Y.Z, got {value!r}')
    return tuple(map(int, value.split('.')))


def next_version(tags, published, initial=''):
    tagged = {t[1:] for t in tags if re.fullmatch('v' + SEMVER, t)}
    stable = {v for v in published if re.fullmatch(SEMVER, v)}
    if tagged - stable:
        raise ValueError(f'Tags not published on Central: {sorted(tagged - stable)}')
    if initial:
        version_key(initial)
        if stable or tagged:
            raise ValueError('initial_version is only permitted with no stable release history')
        return initial
    if not stable:
        raise ValueError('First release requires explicit initial_version (for example 0.1.0)')
    latest = max(stable, key=version_key)
    if tagged and max(tagged, key=version_key) != latest:
        raise ValueError('Central is ahead of tags; reconcile source provenance')
    major, minor, patch = version_key(latest)
    return f'{major}.{minor}.{patch + 1}'


def coordinates():
    values = {}
    for line in (ROOT / 'gradle.properties').read_text().splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            k, v = line.split('=', 1)
            values[k.strip()] = v.strip()
    group, artifact = values['GROUP'], values['POM_ARTIFACT_ID']
    if not re.fullmatch(r'[A-Za-z0-9_]+(?:\.[A-Za-z0-9_]+)+', group):
        raise ValueError('Invalid groupId')
    if not re.fullmatch(r'[A-Za-z0-9_.-]+', artifact):
        raise ValueError('Invalid artifactId')
    return group, artifact


def fetch(url, missing=False, attempts=3):
    for attempt in range(attempts):
        try:
            with urllib.request.urlopen(url, timeout=20) as response:
                return response.read()
        except urllib.error.HTTPError as error:
            if error.code == 404 and missing:
                return None
            if error.code != 429 and error.code < 500:
                raise
            if attempt == attempts - 1:
                raise
        except (urllib.error.URLError, TimeoutError):
            if attempt == attempts - 1:
                raise
        time.sleep(2 ** attempt)


def artifact_base(group, artifact):
    return f'{CENTRAL}/{group.replace(".", "/")}/{artifact}'


def published_versions(group, artifact):
    data = fetch(artifact_base(group, artifact) + '/maven-metadata.xml', missing=True)
    if data is None:
        return []
    xml = ET.fromstring(data)
    if xml.tag != 'metadata' or xml.findtext('groupId') != group or xml.findtext('artifactId') != artifact:
        raise ValueError('Invalid Central metadata identity')
    versions = xml.findall('./versioning/versions/version')
    if not versions or any(not node.text for node in versions):
        raise ValueError('Malformed/empty Central metadata')
    return [node.text for node in versions]


def verify_pom(data, group, artifact, version):
    xml = ET.fromstring(data)
    for node in xml.iter():
        node.tag = node.tag.split('}')[-1]
    if tuple(xml.findtext(k) for k in ('groupId', 'artifactId', 'version')) != (group, artifact, version):
        raise ValueError('POM coordinates/version mismatch')
    if xml.findtext('packaging') != 'aar':
        raise ValueError('Publication must be an Android AAR')
    for path in ('name', 'description', 'url', 'licenses/license/name', 'licenses/license/url',
                 'developers/developer/id', 'developers/developer/name', 'scm/url', 'scm/connection'):
        if not xml.findtext(path):
            raise ValueError(f'Missing POM metadata: {path}')
    dependencies = xml.findall('dependencies/dependency')
    catalog = tomllib.loads((ROOT / 'gradle/libs.versions.toml').read_text())
    expected_dependencies = [('com.google.accompanist', 'accompanist-permissions', catalog['versions']['accompanistPermissions'])]
    for expected in expected_dependencies:
        if not any(tuple(n.findtext(k) for k in ('groupId', 'artifactId', 'version')) == expected
                   and n.findtext('scope', 'compile') == 'compile' for n in dependencies):
            raise ValueError(f'Public dependency must be exported with compile scope: {expected}')
    if any(n.findtext('artifactId') == 'unspecified' or n.findtext('version') == 'unspecified'
           for n in dependencies):
        raise ValueError('Unresolved project dependency in POM')


def verify_artifact(data, suffix, group, artifact, version):
    if suffix == '.pom':
        verify_pom(data, group, artifact, version)
    elif suffix == '.module':
        module = json.loads(data)
        if tuple(module.get('component', {}).get(k) for k in ('group', 'module', 'version')) != (group, artifact, version):
            raise ValueError('Gradle module coordinates mismatch')
        variants = module.get('variants', [])
        api = [v for v in variants if v.get('attributes', {}).get('org.gradle.usage') == 'java-api']
        if not api:
            raise ValueError('Gradle metadata lacks an API consumption variant')
        catalog = tomllib.loads((ROOT / 'gradle/libs.versions.toml').read_text())
        expected = catalog['versions']['accompanistPermissions']
        for variant in api:
            deps = [d for d in variant.get('dependencies', []) if (d.get('group'), d.get('module')) == ('com.google.accompanist', 'accompanist-permissions')]
            if len(deps) != 1 or deps[0].get('version') != {'requires': expected}:
                raise ValueError('Gradle metadata must export the pinned public permission dependency')

    else:
        with zipfile.ZipFile(io.BytesIO(data)) as archive:
            if archive.testzip():
                raise ValueError('Corrupt JAR')
            names = archive.namelist()
            if suffix == '.aar':
                if 'AndroidManifest.xml' not in names or 'classes.jar' not in names:
                    raise ValueError('AAR is missing manifest or classes.jar')
                with zipfile.ZipFile(io.BytesIO(archive.read('classes.jar'))) as classes_jar:
                    classes = [n for n in classes_jar.namelist()
                               if n.endswith('.class') and n.startswith('com/apexfission/android/permission/')]
                    if not classes or any(classes_jar.read(n)[:4] != b'\xca\xfe\xba\xbe' or
                                          int.from_bytes(classes_jar.read(n)[6:8], 'big') != 61 for n in classes):
                        raise ValueError('Missing library classes or unexpected JVM bytecode target')
            elif suffix == '-sources.jar' and not any(n.endswith('.kt') for n in names):
                raise ValueError('Sources JAR has no Kotlin source')
            elif suffix == '-javadoc.jar' and not any(n.endswith(('.md', '.html')) for n in names):
                raise ValueError('Documentation JAR has no documentation')


def outputs(values):
    text = ''.join(f'{k}={v}\n' for k, v in values.items())
    print(text, end='')
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'], 'a') as stream:
            stream.write(text)
    return values

def zip_contents(data):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        if len(archive.namelist()) != len(set(archive.namelist())):
            raise ValueError('Duplicate archive entries')
        return {name: hashlib.sha256(archive.read(name)).hexdigest() for name in archive.namelist()
                if not name.endswith('/') and name != 'META-INF/MANIFEST.MF'}
