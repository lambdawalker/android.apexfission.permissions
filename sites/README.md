# Documentation website

The standalone `android-repo-docs` executable prepares bilingual, versioned content and Zola 0.23.6 renders the same documentation theme as Demo Common. Install the pinned CLI with `./docs.sh --help`, and put checksum-verified Zola 0.23.6 on PATH. From the repository root:

```sh
./docs.sh build --check
sh ./docs.sh sources check
sh ./docs.sh screenshots manifest
```

The site build requires the pinned docs executable, Zola and Git with full history. The launcher downloads a verified release binary on supported platforms. No Python, Astro or npm installation is required to render the site. The docs executable checks source examples and screenshot manifests and compares decoded PNG pixels. Render Android candidates with `sh ./docs.sh screenshots render`, compare with `sh ./docs.sh screenshots check`, and explicitly accept reviewed changes with `sh ./docs.sh screenshots update`. Node.js 24 is used only for site navigation tests.

Output is `sites/dist/`, served at `/android.apexfission.permissions/`. For a local static server, build with `--base` matching its exposed directory. The generated Documentation workflow validates PRs and deploys main to GitHub Pages; trusted successful publication also triggers it. Retry Documentation for a site failure without publishing another Android artifact.

| Content | Source |
| --- | --- |
| English consumer guidance | `docs/agents/`, including the preserved website overview, demos and gallery |
| Spanish guidance and reviewed source hashes | `docs/es/` and `docs/es/translations.json` |
| Current installation | Generated `IMPORT.md`, sourced from `docs/templates/MODULE_IMPORT.md.template` |
| Historical identities | `docs/releases/history/permission/` |
| Quickstart sample | `CameraQuickstartActivity.kt`; check with `sources check`, accept intentional changes with `sources update` |
| Reviewed screenshots | `docs/screenshots/manifest.json` and its README |
| Zola settings and layout | `sites/zola.toml`, `sites/templates/`, `sites/static/` |
| Shared rendering and theme | `gradle.android-repo-setup/cli/docs/` |

Home and `/es/` open the numerically newest confirmed exact release. Development is a separate unreleased scope. The version selector retains exact releases. Each release lists only its confirmed publication destinations; version numbers alone do not establish provider equivalence. Legacy Central 0.2.1 retains installation evidence without an invented historical source manual.

Canonical scopes use `/en/permission/<version>/` and `/es/permission/<version>/`, with scoped raw Markdown and `versions.json`. Missing or stale Spanish translations fall back visibly to English from the selected revision. Source and documentation identities remain separate. Historical Markdown is data; builds execute only current rendering code. Missing historical objects fail the build.

Legacy human routes redirect to their development guides. Root `IMPORT.md`, `llms.txt`, `agents/*.md`, screenshots and older version aliases remain available. `sites/static/screenshots/` retains the old screenshot URLs; `screenshots:update` refreshes those copies and manifest validation rejects drift. Do not edit generated output. `build --check` validates local links, anchors, images and raw endpoints, not external availability or Android behavior.

Release preflight runs `android-repo-setup release-validate` with the steps in `.project/verification/checks.json` in a disposable worktree so rendering cannot dirty immutable publication sources. Cleanup, interruption handling and failure propagation are tested in the shared CLI repository. Screenshot pixel and input-hash regressions are tested in the shared docs crate. Full site checks and landing tests run with `sh sites/tests/landing.sh` and the CLI.

Navigation regression tests run against the CLI-generated `sites/dist/docs.js` as part of `sh sites/tests/landing.sh`, covering project base paths for version/language selectors and search.
