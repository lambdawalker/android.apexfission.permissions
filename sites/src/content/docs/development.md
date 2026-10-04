---
title: Build, test, and release
description: Repository layout, screenshot and device checks, Maven Central publishing, and Pages deployment.
---

This page is for maintainers and contributors. Consumers can start with [installation](../getting-started/), [Compose usage](../compose/), or [callbacks](../callbacks/).

## Repository map

| Location | Purpose |
| --- | --- |
| `permission/src/main/java/com/apexfission/android/permission/ui` | Page definitions, carousel, icon strip, visual defaults, timing helper. |
| `permission/src/main/java/com/apexfission/android/permission/requester` | Compose gate, grant snapshot, code-only request and check helpers, recovery message. |
| `permission/src/main/java/com/apexfission/android/permission/recipe` | Permission statuses, location and notification steps, photo picker. |
| `app/src/main/java/.../demo` | Four runnable demo screens hosted by a single `MainActivity`. |
| `permission/src/test` and `permission/src/screenshotTest` | JVM decision tests and Compose visual fixtures. |
| `app/src/androidTest` | Device-level navigation, grant, denial, and Settings recovery tests. |
| `docs/screenshots` | Committed previews used by the README and gallery. |
| `sites` | Astro Starlight site deployed with GitHub Pages. |

## Local checks

Use Android SDK 37. Library Java source/target compatibility is 17 (the demo uses 11), but the checked-in Gradle daemon criteria request Java 25; CI installs Java 17 and relies on daemon toolchain resolution. See the canonical [maintainer instructions](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/AGENTS.md). From the repository root:

```bash
./gradlew :permission:testDebugUnitTest :permission:generatePomFileForMavenPublication :app:assembleDebug
./gradlew :permission:updateDebugScreenshotTest
./gradlew :app:connectedDebugAndroidTest # requires an attached emulator/device
cd sites && npm ci && npm run build
```

The screenshot update task refreshes fixtures; review changes before committing. The [screen gallery](../gallery/) is generated from Compose screenshot tests, not Android system dialogs. The [Render permission screens workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/render-permission-screens.yml) runs the library checks and uploads screenshots.

## Device tests

The [Device permission tests workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/device-permission-tests.yml) runs instrumentation on API 29 and 35 emulators and uploads reports. Tests include two real camera denials, cautious Settings recovery, recipe grant transitions, and demo navigation. A fresh English emulator avoids old request history affecting recovery assertions. Also check manufacturer dialogs and the photo picker on a physical device when relevant.

## Maven Central release

The manual [Publish permission library workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/publish-permission.yml) runs on `main`, chooses the next patch version from release tags, verifies/builds, and publishes signed artifacts automatically. It waits for public Maven Central availability before updating installation documentation and tagging the exact source commit.

See the [release and recovery guide](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/docs/releases.md) for environment secrets, bootstrap behavior, template editing, validation, and interrupted-publication recovery. [IMPORT.md](../installation/) is authoritative for Maven coordinates and the current published version.

## Documentation site deployment

The [Library site workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/deploy-library-site.yml) builds on site or screenshot changes in pull requests. A relevant push to `main` builds and deploys; `workflow_dispatch` is available for a manual run. GitHub Pages must be configured with **GitHub Actions** as its build source. The site is served under `/android.apexfission.permissions/`; the `github-pages` environment is separate from `maven-central`. Build locally with `cd sites && npm ci && npm run build`. Keep site pages, public KDocs, and the [public API reference](../reference/) aligned when an API changes.
