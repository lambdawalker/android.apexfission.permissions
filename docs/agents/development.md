# Build, test, and release

This page is for maintainers and contributors. Consumers can start with [installation](../getting-started/), [Compose usage](../compose/), or [callbacks](../callbacks/).

## Repository map

| Location | Purpose |
| --- | --- |
| `permission/src/main/java/com/apexfission/android/permission/ui` | Page definitions, carousel, icon strip, visual defaults, timing helper. |
| `permission/src/main/java/com/apexfission/android/permission/requester` | Compose gate, grant snapshot, code-only request and check helpers, recovery message. |
| `permission/src/main/java/com/apexfission/android/permission/recipe` | Permission statuses, location and notification steps, photo picker. |
| `app/src/main/java/.../demo` | Demo Common catalog and five feature screens plus the complete quickstart Activity, launched from `MainActivity`. |
| `permission/src/test` and `permission/src/screenshotTest` | JVM decision tests and Compose visual fixtures. |
| `app/src/androidTest` | Device-level navigation, grant, denial, and Settings recovery tests. |
| `docs/screenshots` | Committed previews used by the README and gallery. |
| `sites` | Zola documentation site deployed with GitHub Pages. |

## Local checks

Use Android SDK 37. The demo app requires API 28 to consume the published Demo Common components; the permission library still supports API 24. Library Java source/target compatibility is 17 (the demo uses 11), but the checked-in Gradle daemon criteria request Java 25; generated CI installs Java 17 and 25. See the canonical [maintainer instructions](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/AGENTS.md). From the repository root:

```bash
./gradlew :permission:testDebugUnitTest :permission:generatePomFileForMavenPublication :app:assembleDebug
cd sites && npm run screenshots:render && npm run screenshots:check && cd ..
./gradlew :app:connectedDebugAndroidTest # requires an attached emulator/device
./setup.sh build_docs --check
```

Install site dependencies with `cd sites && npm ci` before the screenshot commands. Rendering creates candidates; the check compares them with accepted images. After visual review, intentionally export them with `npm run screenshots:update` from `sites/`. CI never accepts changed images automatically. The [screen gallery](../gallery/) is generated from Compose screenshot tests, not Android system dialogs. The [Render permission screens workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/render-permission-screens.yml) runs the library checks and uploads screenshots.

## Device tests

The [Device permission tests workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/device-permission-tests.yml) runs instrumentation on API 29 and 35 emulators and uploads reports. Tests include two real camera denials, cautious Settings recovery, recipe grant transitions, and demo navigation. A fresh English emulator avoids old request history affecting recovery assertions. Also check manufacturer dialogs and the photo picker on a physical device when relevant.

## Releases and recovery

The manual **Publish** workflow selects Maven Central or JitPack and uses destination-scoped source tags and confirmation records. Installation instructions list the destinations that confirmed the selected version. Installation documentation and the persistent documentation catalog advance only after the destination confirms availability.

Use **SUB - Finalize Permissions release** for interrupted confirmation and bookkeeping recovery without uploading artifacts again. See the [release and recovery guide](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/docs/releases.md) for workflow inputs, environment secrets, immutable tags, bootstrap evidence and recovery. [IMPORT.md](../installation/) remains authoritative for confirmed consumer coordinates.

## Documentation site deployment

The [Documentation workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/documentation.yml) builds on site or screenshot changes in pull requests. A relevant push to `main` builds and deploys; `workflow_dispatch` is available for a documentation-only retry. Successful trusted publication or finalization completion also rebuilds the latest `main` catalog, including JitPack confirmation, without relying on a bot commit triggering a push workflow. GitHub Pages must be configured with **GitHub Actions** as its build source. The site is served under `/android.apexfission.permissions/`; the `github-pages` environment is separate from `maven-central`. Build locally with `./setup.sh build_docs --check`. The [site maintenance guide](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/sites/README.md) identifies editable sources. The API reference, concepts, limitations, troubleshooting, migration, task recipes, and first request are generated from canonical Markdown. Edit those sources, not generated site pages. Keep them aligned with public KDocs.

## Documentation and release checks

`npm run check` validates the publisher, quickstart extraction, image manifest, production site, links, anchors, and raw Markdown endpoints. Relevant pull requests run these checks. Screenshot-input changes also render candidates and compare pixels. Before ordinary artifact upload, release preflight checks the site and freshly rendered screenshots at the selected source SHA. Recovery does not repeat this work or upload the package again. A site deployment can be retried independently.
