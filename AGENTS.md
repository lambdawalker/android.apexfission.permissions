# Repository maintenance instructions

This file instructs agents working **on** this repository. It does not declare an autonomous agent or service. Consumer integrations start at [docs/agents/index.md](docs/agents/index.md).

## Map

- `PermissionHandler.kt`: Compose gate and batch runtime request launcher.
- `PermissionBundleScreen.kt`: overview, batch carousel, icon strip, display modes, request action.
- `PermissionAutoAdvance.kt`: reading pace estimator and looping page progression.
- `PermissionDescription.kt`: required host page and icon strip metadata.
- `PermissionState.kt`: shared status inference. `PermanentlyDenied` is a best-effort inference from history and Android rationale.
- `PermissionRecovery.kt`: cautious default recovery note for inferred blocked permissions; callers can replace it and control the Settings action.
- `PermissionGrants.kt`: required/optional grant snapshot passed to protected content.
- `PermissionCheck.kt`: synchronous Context check and infix `otherwise`.
- `PermissionRequester.kt`: Activity-owned launcher and infix `onDenied`.
- `PermissionRecipes.kt`: foreground/background location and notification next-step decisions; see [platform recipes](docs/agents/platform-recipes.md).
- `VisualMediaPicker.kt`: AndroidX photo picker wrapper for user-selected images and videos, with no storage permission.
- `app/src/main/java/com/apexfission/android/permissions/MainActivity.kt`: single sample launcher Activity; carousel, platform recipes, code-only callback, and hero artwork demo composables live in its `demo/` package.
- `sites/`: Astro Starlight documentation site; [its README](sites/README.md) covers local builds and GitHub Pages deployment. It copies screenshot PNGs from `docs/screenshots/` during the build.

Library files above live in `permission/src/main/java/com/apexfission/android/permission/`. Keep KDocs, [Compose guide](docs/agents/compose.md), [code-only guide](docs/agents/code-only.md), and [README](README.md) aligned with public behavior. Never launch a request during composition. Only required permissions gate content; the full list remains available for batch requests and grant snapshots even when `MissingOnly` filters the carousel. The default overview is a generic lock-icon `DefaultPermissionPage`; a caller-created `PermissionOverview` must provide a page. `Automatic` shows the overview only when more than one permission is visible, `Show` always displays it, and `Hide` omits it.

Autoplay is opt-in. A user gesture must latch it off even if `MissingOnly` rebuilds the visible pager after a partial grant. Its segmented indicator follows visible pages, preserves current progress on pause, fades when paused, and resets on wraparound. Do not infer text by traversing semantics; callers set page delays explicitly or use `estimateReadingDelayMillis` with their copy. Keep auto movement inactive while the host is paused and while touch exploration is enabled.

## Verification

```bash
./gradlew :permission:testDebugUnitTest :permission:generatePomFileForMavenPublication :app:assembleDebug
```

Unit tests are in `permission/src/test/`; visual fixtures are in `permission/src/screenshotTest/`. [Render permission screens](.github/workflows/render-permission-screens.yml) runs them in GitHub Actions and uploads PNGs. The [README gallery](README.md#screenshots) uses committed PNGs; follow the [screenshot mapping and refresh steps](docs/screenshots/README.md) whenever the UI changes. Screenshots do not test system dialogs. For launcher changes, test grant, partial denial, settings recovery, and Activity recreation on a device or emulator.

Run `./gradlew :app:connectedDebugAndroidTest` with an emulator or device attached. [Device permission tests](.github/workflows/device-permission-tests.yml) runs the app instrumentation suite on API 29 and 35 emulators and uploads the test reports. The API 35 recovery test handles two real camera denials, checks the cautious recovery message, opens Settings, and returns. Other tests inspect location and notification recipes against device grant state and navigate both demos. The UI test assumes an English emulator and a fresh app install; use a fresh AVD if permission history from an earlier run persists. The device suite does not replace manual checks of OEM permission dialogs and the photo picker on physical devices.

## Installation and released versions

[IMPORT.md](IMPORT.md) is authoritative for the current published version, Maven coordinates, and Gradle Kotlin DSL, Groovy, Maven, and version-catalog installation. Read it when answering installation, importing, dependency, coordinate, or latest-version questions. Do not infer, guess, or hard-code the library version.

Edit `docs/templates/IMPORT.md.template`, then run `./gradlew generateImportDocs verifyImportDocs`; never edit the generated file directly. Normal regeneration preserves its confirmed release version. Publishing alone advances it after Central confirmation. Run `python3 -m unittest discover -s scripts/tests -v` when changing release tooling. The [release guide](docs/releases.md) covers version progression, setup, provenance, and recovery. Never commit signing material.

## Toolchain and change policy

The checked-in build uses compileSdk 37, minSdk 24, Gradle 9.6.0, AGP 9.4.1 and Kotlin Compose plugin 2.2.10. Library Java source/target compatibility is 17 (the demo uses 11); `gradle/gradle-daemon-jvm.properties` separately requests a Java 25 daemon. CI installs Java 17 and relies on Gradle daemon toolchain resolution. Install the Android SDK and configure `ANDROID_HOME` or an untracked `local.properties`. Do not silently downgrade versions to fit a local environment.

Run `./gradlew :permission:lintDebug :app:lintDebug` for Android lint. No dedicated formatter or API binary-compatibility gate is configured; do not claim those checks ran. For documentation-only work run `cd sites && npm ci && npm run check`. For UI changes run `npm run screenshots:render` and `npm run screenshots:check` from `sites/`; accept reviewed image changes only with `npm run screenshots:update`. Android behavior changes also require the unit, build, screenshot and relevant device checks above. Report environmental blockers explicitly.

Public APIs are in the `requester`, `ui`, and `recipe` packages listed above. Kotlin `internal` helpers, private composables and app demo functions are implementation details. Preserve package names, signatures, defaults, enum values and observable callback timing unless an intentional breaking change is requested. Update KDoc, the canonical [API reference](docs/agents/api.md), relevant recipes, migration guidance, demo and tests together. Keep Android permissions in the host manifest; never equate a local history heuristic with platform certainty.

Do not edit `build/`, `sites/dist/`, `sites/.astro/`, generated site assets, or screenshot reference PNGs by hand. Refresh screenshots with the documented Gradle workflow. Dependency lockfiles and the Gradle wrapper should change only with intentional toolchain/dependency updates. Never commit `local.properties`, signing keys, passwords or downloaded SDK files.

`docs/agents/` is canonical consumer guidance; `agents/` and `AI_INTEGRATION_GUIDE.md` preserve old entry links. The site publishes these Markdown files from source during its build. Keep `llms.txt` concise. This is a standalone library: no external architecture repository is designated here. Do not invent a dependency on an unrelated system-design repository.

## Documentation source ownership

The human API reference, concepts, limitations, troubleshooting, migration, task recipes and first request are generated from `docs/agents/`. Do not edit their generated `sites/src/content/docs/` copies. Quickstart code comes from `CameraQuickstartActivity.kt`; run `node sites/scripts/sync-examples.mjs --write` after intentional edits. `sites/README.md` maps all source ownership and validation commands. `docs/screenshots/manifest.json` records curated captures; normal CI validates rather than accepts images.
