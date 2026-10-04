import { cp, mkdir, readdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const site = resolve(fileURLToPath(new URL('..', import.meta.url)));
const source = resolve(site, '../docs/screenshots');
const target = resolve(site, 'public/screenshots');
await mkdir(target, { recursive: true });
for (const filename of await readdir(source)) {
  if (filename.endsWith('.png')) await cp(resolve(source, filename), resolve(target, filename));
}

// Publish canonical agent Markdown without maintaining a second documentation copy.
// Relative links become repository URLs so source/examples remain reachable on Pages.
const { readFile, writeFile } = await import('node:fs/promises');
const agentSource = resolve(site, '../docs/agents');
const agentTarget = resolve(site, 'public/agents');
await mkdir(agentTarget, { recursive: true });
for (const filename of await readdir(agentSource)) {
  if (!filename.endsWith('.md')) continue;
  const markdown = await readFile(resolve(agentSource, filename), 'utf8');
  const published = markdown.replace(/\]\(([^)]+)\)/g, (match, href) => {
    if (/^(?:[a-z]+:|#|\/)/i.test(href)) return match;
    return `](${new URL(href, 'https://github.com/lambdawalker/android.apexfission.permissions/blob/main/docs/agents/').href})`;
  });
  await writeFile(resolve(agentTarget, filename), published);
}

// Publish the one committed installation document as both Markdown and a site page.
const installation = await readFile(resolve(site, '../IMPORT.md'), 'utf8');
await writeFile(resolve(site, 'public/IMPORT.md'), installation);
await writeFile(resolve(site, 'src/content/docs/installation.md'),
  '---\ntitle: Installation\ndescription: Current published version and dependency setup, generated from IMPORT.md.\n---\n\n' + installation);
