# Agent guide

This repository contains a reusable Android Compose camera permission gate.

- Gradle module: `:permission`; Kotlin package: `com.apexfission.android.permission`.
- `CameraPermissionState.kt`: public `CameraPermissionStatus`, `CameraPermissionController`, `rememberCameraPermissionController`; state resolution and request history.
- `PermissionHandler.kt`: `HandleCameraPermission` gate. The default UI uses `PermissionScreen` and only invokes request on a button tap.
- `PermissionScreen.kt`: optional Material 3 primer UI and `PermissionFeature` model.
- `PermissionViewModel.kt`: Accompanist camera permission adapter. Public methods expose `PermissionState`.
- `app`: minimal consumer example; declare `android.permission.CAMERA` in the consuming app's manifest.

Integration: add `implementation(project(":permission"))`, import `com.apexfission.android.permission.HandleCameraPermission`, and supply `onBack`, `onNotNow`, and granted content. For custom UI, handle all four enum values and invoke `requestPermission` or `openAppSettings` on an explicit user action. Do not request permission during composition.

Keep the public API documented with KDoc when changing it. Preserve the distinction between Android's rationale signal and the stored request-history heuristic. Use `./gradlew :permission:testDebugUnitTest :permission:assembleDebug :app:assembleDebug` to verify changes. No artifact publication is configured in this repository; do not claim a Maven coordinate is available.

For images, see `permission/src/screenshotTest/` and `.github/workflows/render-permission-screens.yml`. Run `:permission:updateDebugScreenshotTest` to render PNGs; the workflow uploads them as an artifact. Static previews do not exercise system permission dialogs.
