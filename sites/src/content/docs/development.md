---
title: Build, test, and release
description: Repository layout, screenshot and device checks, Maven Central staging, and Pages deployment.
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

Use a compatible Android SDK and JDK 17. From the repository root:

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

The `:permission` artifact uses `com.apexfission.androi:permission:<version>`. The publishing workflow is manual, runs only on `main`, builds/tests, signs, and **stages** the artifact; a maintainer finishes publishing in Central Portal. Before running it:

1. Verify ownership of the Maven Central namespace `com.apexfission.androi` and create a Central Portal user token.
2. Create a GPG signing key and export its ASCII-armored private key. Keep the key and passphrase private.
3. In the GitHub `maven-central` environment set `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `SIGNING_IN_MEMORY_KEY`, and `SIGNING_IN_MEMORY_KEY_PASSWORD`. The first pair comes from the Central Portal token.
4. Open [Publish permission library](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/publish-permission.yml), choose **Run workflow** on `main`, and supply the version, for example `0.2.0`.
5. Review the staged deployment in Central Portal and publish it there. Confirm the artifact and update version examples on this site when needed.

The artifact is Apache-2.0 licensed; POM metadata includes the project URL, developer, issue tracker, and SCM coordinates. Never commit signing material. The version passed to the workflow overrides the build's fallback `permissionVersion` property.

## Documentation site deployment

The [Library site workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/deploy-library-site.yml) builds on site or screenshot changes in pull requests. A relevant push to `main` builds and deploys; `workflow_dispatch` is available for a manual run. GitHub Pages must be configured with **GitHub Actions** as its build source. The site is served under `/android.apexfission.permissions/`; the `github-pages` environment is separate from `maven-central`. Build locally with `cd sites && npm ci && npm run build`. Keep site pages, public KDocs, and the [public API reference](../reference/) aligned when an API changes.
