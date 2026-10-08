# Library website

Astro Starlight builds the human documentation. `docs/agents/` owns consumer contracts and `IMPORT.md` owns confirmed installation facts. Legacy routes show development documentation for `main`; versioned routes identify confirmed release snapshots separately. The banner identifies the selected scope.

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
| Legacy human feature guides, overview, demos, gallery, development | `sites/src/content/docs/` non-generated pages |
| Versioned consumer guides / Spanish prose | `docs/agents/` / `docs/es/`; localized site routes are generated |
| Appearance/version banner/navigation | `src/styles/brand.css`, `src/components/VersionBanner.astro`, `astro.config.mjs` |
| Screenshot mapping and accepted assets | `docs/screenshots/manifest.json`; follow its README |

The build publishes canonical Markdown recursively under `/agents/*.md`, preserving Markdown-to-Markdown navigation and linking source files to GitHub. It also publishes `/IMPORT.md` and `/llms.txt`. Markdown parsing preserves code examples and reference links. Generated raw asset directories are cleared before synchronization; the generated-page registry removes obsolete human outputs. Never edit generated copies.

## Deployment

Output is `sites/dist/`, served at `/android.apexfission.permissions/`. The Library site workflow validates relevant PRs and deploys relevant `main` pushes or a manual run. A successful library publishing workflow also triggers it to pick up confirmed installation changes. Set repository Settings → Pages → Build and deployment to GitHub Actions. The `github-pages` environment is separate from Maven Central.

Documentation-only changes do not require an emulator. Changes to screenshot inputs run the separate Render permission screens workflow. Ordinary package release preflight validates the exact release source before upload; recovery skips new screenshot generation and never reuploads the package. A site failure is retried through Library site, not by publishing another package.


## Versioned English and Spanish documentation

Canonical English remains in `docs/agents/`. Translate its prose into `docs/es/`, preserve code fences and heading structure, and review before recording the English file SHA-256 in `docs/es/translations.json`. Missing or stale translations visibly fall back to English from the selected documentation revision; they never borrow current translations for an old release. English heading anchors remain available in Spanish HTML and raw Markdown.

`npm run check` builds all cataloged releases and the working development guides under `/en/permission/<version>/` and `/es/permission/<version>/`, with matching `raw/*.md` endpoints. `versions.json` and the localized catalogs provide discovery without scraping selectors. Version/language controls retain the guide when available and explain an index fallback. The single library has no module selector. Existing human routes, screenshots and raw agent routes remain available.

`scripts/documentation_history.py export` supplies exact-version installation and confirmed immutable source/documentation identities. The build reads historical Markdown with Git and runs only current renderer code; fetch full history. Missing objects fail the build. It cleans generated localized routes and production output before assembly. Do not edit ignored generated pages, `src/versions.json`, `src/build.json`, or localized public files.

The initial archive is empty: legacy Central 0.2.1 installation lacks a verified source identity and is not falsely labeled a historical manual. Future confirmed releases with retained identities form the archive boundary. A reviewed documentation correction keeps the code identity unchanged and records documentation/translation identity separately; see the release guide for catalog operations. Translation trees must be retained by merged Git content, not dangling PR commits.

The Library site workflow fetches full history and uses latest `main` after trusted successful publication/finalization completion, including JitPack. Its manual dispatch retries documentation only. No package publication is required for translation updates. Link validation covers select options, localized alternates, scoped raw Markdown and anchors, and dotted version slugs. Automated HTML validation does not replace keyboard, desktop or narrow-screen visual review.
