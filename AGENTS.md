# Agent guide

This repository is a Compose library for Android runtime permission explanations and requests.

- Module `:permission`, package `com.apexfission.android.permission`.
- `PermissionState.kt`: generic status, controller, per-permission request history and pure flow decisions.
- `PermissionHandler.kt`: `HandlePermissions` gate, which shows protected content only when all statuses are granted; camera-only compatibility wrapper.
- `PermissionScreen.kt`: `PermissionDescription`, optional per-item composable hero, wrapping icon selector, horizontal pager, and selected-page action.
- `CameraPermissionState.kt` and `PermissionViewModel.kt`: camera compatibility types and Accompanist adapter.
- `app`: two-permission sample; app manifest declares CAMERA and RECORD_AUDIO.
- `permission/src/test/`: state/flow unit tests. `permission/src/screenshotTest/`: static visual fixtures.

The host owns the list order and must declare every requested runtime permission in its manifest. `HandlePermissions` is the default batch gate, delegating to `HandlePermissionBundle`: one button requests the set after the overview/permission carousel. `HandlePermissionsIndividually` retains the old sequential path. No request launches from composition. `PermissionDescription` has a trailing `description` composable for the area beneath the icon strip and a separate `hero` slot; keep selector `label`/`icon` metadata in the description model. `PermissionOverview` provides the general first page for multiple permissions. The horizontally scrolling icon strip is below each hero, with an equal style on overview and active style on permission pages. Keep generic defaults localized and public API documented with KDoc. Special app access requires a distinct Android flow.

Run `./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :app:assembleDebug`. The GitHub render workflow uploads PNGs; previews do not test system dialogs. No Maven coordinate is published.
