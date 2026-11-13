# Documentation website

The documentation CLI pinned in `.project/docs/config.yaml` owns the Astro/Starlight renderer, Markdown preparation, route generation and search. Install it through `./docs.sh --help`; builds require Node.js 24, npm and Git with full history. The current `sites/static/` assets and legacy aliases are copied by the shared build. No consumer npm renderer is maintained. Run from the repository root:

```sh
./docs.sh build --check
sh sites/tests/landing.sh
```

Output is `sites/dist/`, served at `/android.apexfission.permissions/`. For a local static server, build with `--base` matching its exposed directory. The generated Documentation workflow validates PRs and deploys main to GitHub Pages; trusted successful publication also triggers it. Retry Documentation for a site failure without publishing another Android artifact.

| Content | Source |
| --- | --- |
| English consumer guidance | `docs/agents/`, including the preserved website overview, demos and gallery |
| Spanish guidance and reviewed source hashes | `docs/es/` and `docs/es/translations.json` |
| Current installation | Generated `IMPORT.md`, sourced from `docs/templates/MODULE_IMPORT.md.template` |
| Historical identities | `docs/releases/history/permission/` |
| Quickstart sample | `CameraQuickstartActivity.kt`; check with `sources check`, accept intentional changes with `sources update` |
| Reviewed screenshots | `docs/screenshots/manifest.json` and its README |
| Astro settings and presentation | `.project/docs/config.yaml`, `sites/astro/components/`, `sites/astro/public/` |
| Shared rendering and theme | `gradle.android-repo-setup/cli/docs/` |

Home and `/es/` open the numerically newest confirmed exact release. Development is a separate unreleased scope. The version selector retains exact releases. Each release lists only its confirmed publication destinations; version numbers alone do not establish provider equivalence. Legacy Central 0.2.1 retains installation evidence without an invented historical source manual.

Canonical scopes use `/en/permission/<version>/` and `/es/permission/<version>/`, with scoped raw Markdown and `versions.json`. Missing or stale Spanish translations fall back visibly to English from the selected revision. Source and documentation identities remain separate. Historical Markdown is data; builds execute only current rendering code. Missing historical objects fail the build.

Legacy human routes redirect to their development guides. Root `IMPORT.md`, `llms.txt`, `agents/*.md`, screenshots and older version aliases remain available. `sites/static/screenshots/` retains the old screenshot URLs; `screenshots:update` refreshes those copies and manifest validation rejects drift. Do not edit generated output. `build --check` validates local links, anchors, images and raw endpoints, not external availability or Android behavior.

Release preflight runs `android-repo-setup release-validate` with the steps in `.project/verification/checks.json` in a disposable worktree so rendering cannot dirty immutable publication sources. Cleanup, interruption handling and failure propagation are tested in the shared CLI repository. Screenshot pixel and input-hash regressions are tested in the shared docs crate. Full site checks and output tests run with `sh sites/tests/landing.sh` and the CLI.

Navigation regressions inspect the generated Astro HTML, guide-preserving selector destinations, scoped search metadata, raw endpoints and legacy aliases. Screenshot candidate rendering and acceptance remain separate from website rendering.

Legacy screenshots remain owned by `sites/static/screenshots/`; the shared Astro build copies current static assets and leaves the existing screenshot lifecycle authoritative.

The CLI pins use corrective source revision `1da53392b262c7c3129d2bd645dfd7b34ead81e1`, built from the shared CLI after v0.1.21. This revision preserves current static assets and aliases and supports Astro builds across filesystems; the official v0.1.21 release alone does not include these fixes. Full source pins require Rust/Cargo.
