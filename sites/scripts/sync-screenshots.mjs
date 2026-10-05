// Compatibility entry point: synchronize all canonical documentation assets.
import { cp, mkdir, readdir, readFile, writeFile, rm } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { resolve, dirname } from 'node:path';
import { rewriteMarkdown } from './markdown.mjs';

const site = resolve(fileURLToPath(new URL('..', import.meta.url)));
const repo = resolve(site, '..');
async function write(path, text) { await mkdir(dirname(path), {recursive:true}); await writeFile(path,text); }
async function walk(directory, prefix = '') {
  const files = [];
  for (const entry of await readdir(directory, {withFileTypes:true})) {
    const path = prefix + entry.name;
    if (entry.isDirectory()) files.push(...await walk(resolve(directory,entry.name), path + '/'));
    else files.push(path);
  }
  return files;
}
for (const dir of ['screenshots', 'agents']) {
  await rm(resolve(site,'public',dir), {recursive:true,force:true});
  await mkdir(resolve(site,'public',dir), {recursive:true});
}
for (const file of await walk(resolve(repo,'docs/screenshots'))) {
  if (!file.endsWith('.png')) continue;
  const target = resolve(site,'public/screenshots',file);
  await mkdir(dirname(target),{recursive:true});
  await cp(resolve(repo,'docs/screenshots',file),target);
}
for (const file of await walk(resolve(repo,'docs/agents'))) {
  if (!file.endsWith('.md')) continue;
  const source = `docs/agents/${file}`;
  await write(resolve(site,'public/agents',file), rewriteMarkdown(await readFile(resolve(repo,source),'utf8'), source));
}
const generated = {
  'reference': ['api.md', 'Public API reference'],
  'concepts': ['concepts.md', 'Concepts, lifetime and ownership'],
  'limitations': ['limitations.md', 'Limitations and security boundaries'],
  'troubleshooting': ['troubleshooting.md', 'Troubleshooting'],
  'migration': ['migration.md', 'Migration'],
  'task-recipes': ['recipes.md', 'Task recipes'],
  'getting-started': ['quickstart.md', 'First request'],
};
// Remove only outputs owned by this generator, including pages removed from its map.
const registry = resolve(site,'.generated-doc-pages.json');
let previous = [];
try { previous = JSON.parse(await readFile(registry,'utf8')); } catch (error) { if (error.code !== 'ENOENT') throw error; }
for (const slug of previous) {
  if (!/^[a-z-]+$/.test(slug)) throw new Error('Invalid generated page registry');
  await rm(resolve(site,`src/content/docs/${slug}.md`),{force:true});
}
for (const [slug,[file,title]] of Object.entries(generated)) {
  const source = `docs/agents/${file}`;
  let content = await readFile(resolve(repo,source),'utf8');
  content = rewriteMarkdown(content.replace(/^# [^\n]+\n/,''), source, 'human');
  await write(resolve(site,`src/content/docs/${slug}.md`), `---\ntitle: ${title}\n---\n\n<!-- Generated from ${source}; do not edit. -->\n\n${content}`);
}
const installation = await readFile(resolve(repo,'IMPORT.md'),'utf8');
await write(resolve(site,'public/IMPORT.md'), rewriteMarkdown(installation, 'IMPORT.md'));
await write(resolve(site,'src/content/docs/installation.md'), '---\ntitle: Installation\n---\n\n' + rewriteMarkdown(installation.replace(/^# Install Apexfission Permissions\n/m,''), 'IMPORT.md', 'human'));
await writeFile(registry, JSON.stringify([...Object.keys(generated),'installation']));
