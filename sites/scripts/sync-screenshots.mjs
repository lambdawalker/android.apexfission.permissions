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
