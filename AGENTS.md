# Agent guide

This repository is a Compose library for Android runtime permission explanations and requests.

- Module `:permission`, package `com.apexfission.android.permission`.
- `PermissionState.kt`: generic status, controller, per-permission request history and pure flow decisions.
- `PermissionHandler.kt`: `HandlePermissions` gate, which shows protected content only when all statuses are granted; camera-only compatibility wrapper.
- `PermissionScreen.kt`: `PermissionDescription`, optional per-item composable hero, wrapping icon selector, horizontal pager, and selected-page action.
- `CameraPermissionState.kt` and `PermissionViewModel.kt`: camera compatibility types and Accompanist adapter.
- `app`: two-permission sample; app manifest declares CAMERA and RECORD_AUDIO.
- `permission/src/test/`: state/flow unit tests. `permission/src/screenshotTest/`: static visual fixtures.

The host owns the list order and must declare every requested runtime permission in its manifest. Do not trigger permission requests from composition. One selected page maps to one explicit request. Keep generic defaults localized, and document new public API with KDoc. Special app access has a distinct Android flow; the pure `PermissionScreen` can present host-managed state/actions, while `HandlePermissions` is for runtime permissions.

Run `./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :app:assembleDebug`. The GitHub render workflow executes these checks and uploads PNGs. Screenshot previews do not test the Android system dialog. No Maven coordinate is published by this repository.

The additive batch experiment lives in `PermissionBundleScreen.kt`; see `docs/batch-request.md`. `HandlePermissionBundle` uses Accompanist's multiple-permission request, while `PermissionBundleScreen` can render host-managed statuses. The separate `BatchPermissionActivity` is a second launcher entry. Keep the original `HandlePermissions` behavior intact when working on this experiment.
