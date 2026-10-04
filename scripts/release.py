"""Release policy and deterministic import docs; Python standard library only."""
import argparse
import os
from pathlib import Path
import re
import subprocess
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SEMVER = r'(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)'
CENTRAL = 'https://repo.maven.apache.org/maven2'


def version_key(version):
    if not re.fullmatch(SEMVER, version):
        raise ValueError(f'Expected stable X.Y.Z version, got {version!r}')
    return tuple(map(int, version.split('.')))


def next_version(tags, published):
    releases = [tag[1:] for tag in tags if re.fullmatch('v' + SEMVER, tag)]
    stable = [v for v in published if re.fullmatch(SEMVER, v)]
    if not stable:
        raise ValueError('No published stable version; bootstrap must be reviewed manually')
    latest = max(stable, key=version_key)
    if releases:
        tagged = max(releases, key=version_key)
        if tagged not in stable:
            raise ValueError(f'Tag v{tagged} is not published on Central')
        if version_key(latest) > version_key(tagged):
            raise ValueError('Central is ahead of release tags; reconcile provenance before publishing')
        latest = tagged
    major, minor, patch = version_key(latest)
    return f'{major}.{minor}.{patch + 1}'


def document_version(document):
    match = re.search(r'<!-- release-version: (\S+) -->', document)
    if not match:
        raise ValueError('IMPORT.md has no release-version marker; supply -PreleaseVersion')
    version_key(match[1])
    return match[1]


def render(template, group, artifact, version):
    version_key(version)
    for key, value in dict(VERSION=version, GROUP_ID=group, ARTIFACT_ID=artifact).items():
        template = template.replace('{{' + key + '}}', value)
    if re.search(r'\{\{.*?\}\}', template):
        raise ValueError('Unknown template placeholder')
    return template


def coordinates():
    properties = {}
    for line in (ROOT / 'gradle.properties').read_text().splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            key, value = line.split('=', 1)
            properties[key.strip()] = value.strip()
    return properties['GROUP'], properties['POM_ARTIFACT_ID']


def fetch(url):
    with urllib.request.urlopen(url, timeout=30) as response:
        return response.read()


def artifact_base(group, artifact):
    return f'{CENTRAL}/{group.replace(".", "/")}/{artifact}'


def published_versions(group, artifact):
    root = ET.fromstring(fetch(artifact_base(group, artifact) + '/maven-metadata.xml'))
    return [node.text for node in root.findall('./versioning/versions/version')]


def verify_pom(data, group, artifact, version):
    root = ET.fromstring(data)
    # Maven POMs normally use a default namespace.
    for node in root.iter():
        node.tag = node.tag.split('}')[-1]
    actual = tuple(root.findtext(key) for key in ('groupId', 'artifactId', 'version'))
    if actual != (group, artifact, version):
        raise ValueError(f'Published POM coordinates do not match: {actual}')


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True).strip()


def prepare(resume):
    tags = git('tag', '--list').splitlines()
    pending = [tag for tag in tags if tag.startswith('release-pending/')]
    if resume:
        version_key(resume)
        if pending != [f'release-pending/{resume}']:
            raise ValueError('Recovery requires exactly the matching pending release reference')
        if f'v{resume}' in tags:
            raise ValueError('Release tag already exists; inspect the completed release')
        source = git('rev-parse', f'refs/tags/release-pending/{resume}^{{commit}}')
        version = resume
    else:
        if pending:
            raise ValueError(f'Unresolved release attempt: {pending}; inspect Central and use resume_version')
        group, artifact = coordinates()
        version = next_version(tags, published_versions(group, artifact))
        source = git('rev-parse', 'HEAD')
        # Prevent publishing identical code again (documentation-only releases still allowed).
        if any(git('rev-parse', f'refs/tags/{tag}^{{commit}}') == source
               for tag in tags if re.fullmatch('v' + SEMVER, tag)):
            raise ValueError('This source commit already has a release tag')
    if subprocess.run(['git', 'merge-base', '--is-ancestor', source, 'origin/main'], cwd=ROOT).returncode:
        raise ValueError('Release source must belong to main history')
    output = f'version={version}\nsource={source}\n'
    print(output, end='')
    with open(os.environ['GITHUB_OUTPUT'], 'a') as stream:
        stream.write(output)


def wait_for_publication(version, timeout):
    version_key(version)
    group, artifact = coordinates()
    base = f'{artifact_base(group, artifact)}/{version}/{artifact}-{version}'
    deadline = time.monotonic() + timeout
    while True:
        try:
            verify_pom(fetch(base + '.pom'), group, artifact, version)
            # Confirm the usable artifact, not just an uploaded or staged POM.
            with urllib.request.urlopen(base + '.aar', timeout=30) as response:
                if not response.read(1):
                    raise ValueError('Published AAR is empty')
            print(f'Confirmed {group}:{artifact}:{version} on Maven Central')
            return
        except urllib.error.HTTPError as error:
            if error.code != 404 and error.code != 429 and error.code < 500:
                raise
            if time.monotonic() >= deadline:
                raise TimeoutError('Publication not confirmed; pending reference retained') from error
        except (urllib.error.URLError, TimeoutError) as error:
            if time.monotonic() >= deadline:
                raise TimeoutError('Central unreachable; pending reference retained') from error
        time.sleep(20)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    for command in ('generate', 'verify'):
        sub = commands.add_parser(command)
        sub.add_argument('--version', default='')
        sub.add_argument('--group')
        sub.add_argument('--artifact')
    sub = commands.add_parser('prepare')
    sub.add_argument('--resume', default='')
    sub = commands.add_parser('wait')
    sub.add_argument('--version', required=True)
    sub.add_argument('--timeout', type=int, default=2400)
    args = parser.parse_args()
    if args.command == 'prepare':
        prepare(args.resume)
    elif args.command == 'wait':
        wait_for_publication(args.version, args.timeout)
    else:
        output = ROOT / 'IMPORT.md'
        version = args.version or document_version(output.read_text())
        group, artifact = coordinates()
        expected = render((ROOT / 'docs/templates/IMPORT.md.template').read_text(),
                          args.group or group, args.artifact or artifact, version)
        if args.command == 'generate':
            output.write_text(expected, encoding='utf-8', newline='\n')
        elif output.read_text() != expected:
            raise ValueError('IMPORT.md is stale; run ./gradlew generateImportDocs and commit the diff')


if __name__ == '__main__':
    main()
