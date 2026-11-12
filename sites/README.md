# Documentation website

The shared `android-repo-setup` CLI v0.1.10 prepares bilingual, versioned content and Zola 0.23.6 renders the same documentation theme as Demo Common. Install the pinned CLI with `./setup.sh --help`, and put checksum-verified Zola 0.23.6 on PATH. From the repository root:

```sh
./setup.sh build_docs --check
node sites/scripts/sync-examples.mjs
node sites/scripts/screenshots.mjs manifest
```

The site build requires Rust tooling, Zola and Git with full history. No Python, Astro or npm installation is required to render the site. Node.js 24 runs the quickstart and screenshot-manifest checks. Pixel comparison and screenshot rendering additionally use `npm --prefix sites ci` to install Sharp, then `npm --prefix sites test`, `npm --prefix sites run screenshots:render` and `npm --prefix sites run screenshots:check`. Only accept reviewed images with `screenshots:update`.

Output is `sites/dist/`, served at `/android.apexfission.permissions/`. For a local static server, build with `--base` matching its exposed directory. The generated Documentation workflow validates PRs and deploys main to GitHub Pages; trusted successful publication also triggers it. Retry Documentation for a site failure without publishing another Android artifact.

| Content | Source |
| --- | --- |
| English consumer guidance | `docs/agents/`, including the preserved website overview, demos and gallery |
| Spanish guidance and reviewed source hashes | `docs/es/` and `docs/es/translations.json` |
| Current installation | Generated `IMPORT.md`, sourced from `docs/templates/MODULE_IMPORT.md.template` |
| Historical identities | `docs/releases/history/permission/` |
| Quickstart sample | `CameraQuickstartActivity.kt`; check with `sync-examples.mjs`, accept intentional changes with `--write` |
| Reviewed screenshots | `docs/screenshots/manifest.json` and its README |
| Zola settings and layout | `sites/zola.toml`, `sites/templates/`, `sites/static/` |
| Shared rendering and theme | `gradle.android-repo-setup/cli/` |

Home and `/es/` open the numerically newest confirmed exact release. Development is a separate unreleased scope. The version selector retains exact releases. No equivalence between provider releases is inferred from version numbers: `release-equivalence.json` is deliberately empty here. Legacy Central 0.2.1 retains installation evidence without an invented historical source manual.

Canonical scopes use `/en/permission/<version>/` and `/es/permission/<version>/`, with scoped raw Markdown and `versions.json`. Missing or stale Spanish translations fall back visibly to English from the selected revision. Source and documentation identities remain separate. Historical Markdown is data; builds execute only current rendering code. Missing historical objects fail the build.

Legacy human routes redirect to their development guides. Root `IMPORT.md`, `llms.txt`, `agents/*.md`, screenshots and older version aliases remain available. `sites/static/screenshots/` retains the old screenshot URLs; `screenshots:update` refreshes those copies and manifest validation rejects drift. Do not edit generated output. `build_docs --check` validates local links, anchors, images and raw endpoints, not external availability or Android behavior.

Release preflight runs `scripts/validate-release-source.sh` in a disposable worktree so rendering cannot dirty immutable publication sources. Its cleanup and failure propagation are tested by `sites/scripts/tests/release-isolation.test.mjs`. Screenshot pixel and input-hash regressions remain in the same test suite. Full site checks and landing tests run with `sh sites/tests/landing.sh` and the CLI.
