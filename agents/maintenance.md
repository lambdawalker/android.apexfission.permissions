# Repository maintenance

## Map

- `PermissionHandler.kt`: Compose gate and compatibility wrappers.
- `PermissionBundleScreen.kt`: overview, batch carousel, icon strip, display modes, request action.
- `PermissionAutoAdvance.kt`: reading pace estimator and one-way page progression.
- `PermissionScreen.kt`: description metadata and individual primer.
- `PermissionState.kt`: status inference and controller actions. `PermanentlyDenied` is a best-effort inference from history and Android rationale.
- `PermissionCheck.kt`: synchronous Context check and infix `otherwise`.
- `PermissionRequester.kt`: Activity-owned launcher and infix `onDenied`.
- `CameraPermissionState.kt` / `PermissionViewModel.kt`: compatibility and Accompanist adapter.
- `app/src/main/java/com/apexfission/android/permissions/MainActivity.kt`: single sample launcher Activity.

Library files above live in `permission/src/main/java/com/apexfission/android/permission/`. Keep KDocs, [Compose guide](compose.md), [code-only guide](code-only.md), and [README](../README.md) aligned with public behavior. Never launch a request during composition. The full permission list gates content even when `MissingOnly` filters the carousel. `PermissionOverviewMode.Automatic` follows the visible permission count; `Show` and `Hide` explicitly override it.

Autoplay is opt-in. A user gesture must latch it off even if `MissingOnly` rebuilds the visible pager after a partial grant. Do not infer text by traversing semantics; callers set page delays explicitly or use `estimateReadingDelayMillis` with their copy. Keep auto movement inactive while the host is paused and while touch exploration is enabled.

## Verification

```bash
./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :permission:generatePomFileForMavenPublication :app:assembleDebug
```

Unit tests are in `permission/src/test/`; visual fixtures are in `permission/src/screenshotTest/`. [Render permission screens](../.github/workflows/render-permission-screens.yml) runs them in GitHub Actions and uploads PNGs. The [README gallery](../README.md#screenshots) uses committed PNGs; follow the [screenshot mapping and refresh steps](../docs/screenshots/README.md) whenever the UI changes. Screenshots do not test system dialogs. For launcher changes, test grant, partial denial, settings recovery, and Activity recreation on a device or emulator.

`:permission` publishes as `com.apexfission.android.permission:core:<version>`. [Publish permission library](../.github/workflows/publish-permission.yml) is manually triggered on `main` and stages a signed deployment. A maintainer publishes it from Central Portal. See [README](../README.md#publish-to-maven-central) for namespace and signing setup; never commit signing material.
