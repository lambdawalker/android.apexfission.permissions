# Library website

Astro Starlight builds the human documentation. `docs/agents/` owns consumer contracts and `IMPORT.md` owns confirmed installation facts. This is development documentation for `main`; the global banner makes clear that source APIs may be newer than the published dependency.

```bash
cd sites
npm ci
npm run dev
npm run check
```

`check` runs publisher/link/image regression tests, verifies the quickstart against its compiled Activity source, checks image manifest hashes, builds the site and search index, and checks output links, anchors, images, base paths, and required raw Markdown endpoints. It does not check external network availability or render Android UI. Run it from `sites/`.

## Editable sources

| Content | Edit here |
| --- | --- |
| API reference, concepts, limitations, troubleshooting, migration, task recipes | Corresponding `docs/agents/*.md` file; matching human pages are generated |
| Complete quickstart code | `app/src/main/java/com/apexfission/android/permissions/demo/CameraQuickstartActivity.kt`, then `node sites/scripts/sync-examples.mjs --write` from the repository root |
| Quickstart explanation | `docs/agents/quickstart.md`, outside extraction markers |
| Installation | `docs/templates/IMPORT.md.template`; use the existing Gradle generation/verification tasks |
| Human feature guides, overview, demos, gallery, development | `sites/src/content/docs/` non-generated pages |
| Appearance/version banner/navigation | `src/styles/brand.css`, `src/components/VersionBanner.astro`, `astro.config.mjs` |
| Screenshot mapping and accepted assets | `docs/screenshots/manifest.json`; follow its README |

The build publishes canonical Markdown recursively under `/agents/*.md`, preserving Markdown-to-Markdown navigation and linking source files to GitHub. It also publishes `/IMPORT.md` and `/llms.txt`. Markdown parsing preserves code examples and reference links. Generated raw asset directories are cleared before synchronization; the generated-page registry removes obsolete human outputs. Never edit generated copies.

## Deployment

Output is `sites/dist/`, served at `/android.apexfission.permissions/`. The Library site workflow validates relevant PRs and deploys relevant `main` pushes or a manual run. A successful library publishing workflow also triggers it to pick up confirmed installation changes. Set repository Settings → Pages → Build and deployment to GitHub Actions. The `github-pages` environment is separate from Maven Central.

Documentation-only changes do not require an emulator. Changes to screenshot inputs run the separate Render permission screens workflow. Ordinary package release preflight validates the exact release source before upload; recovery skips new screenshot generation and never reuploads the package. A site failure is retried through Library site, not by publishing another package.
