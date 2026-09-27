# Agent guide

This repository is a Compose library for Android runtime permission explanations and requests.

- Module `:permission`, package `com.apexfission.android.permission`.
- `PermissionState.kt`: generic status, controller, per-permission request history and pure flow decisions.
- `PermissionCheck.kt`: code-only `Context.runIfPermissionsGranted` vararg/list checks and infix `otherwise` missing branch.
- `PermissionRequester.kt`: Activity-owned result launcher, pending request with infix `onDenied` terminal, and callback coordinator.
- `PermissionHandler.kt`: `HandlePermissions` gate, which shows protected content only when all statuses are granted; camera-only compatibility wrapper.
- `PermissionScreen.kt`: `PermissionDescription`, optional per-item composable hero, wrapping icon selector, horizontal pager, and selected-page action.
- `CameraPermissionState.kt` and `PermissionViewModel.kt`: camera compatibility types and Accompanist adapter.
- `app`: one launcher Activity demonstrating two permissions and a success screen; app manifest declares CAMERA and RECORD_AUDIO.
- `permission/src/test/`: state/flow unit tests. `permission/src/screenshotTest/`: static visual fixtures.

The host owns the list order and must declare every requested runtime permission in its manifest. `HandlePermissions` is the default batch gate, delegating to `HandlePermissionBundle`: one button requests the set after the overview/permission carousel. `HandlePermissionsIndividually` retains the old sequential path. No request launches from composition. `PermissionDescription` has a trailing `page` composable for its full carousel page (hero plus explanation); `PermissionOverview(page = { ... })` provides the general first page for multiple permissions. Legacy `hero` and `description` fields remain usable when `page` is absent. Keep selector `label`/`icon` metadata in the description model. The icon strip stays outside the pager, just above the button, and uses full screen width. It centers the selected permission or centers the whole row on overview, overflowing equally on both sides. Keep generic defaults localized and public API documented with KDoc. Special app access requires a distinct Android flow.

Run `./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :permission:generatePomFileForMavenPublication :app:assembleDebug`. The GitHub render workflow uploads PNGs; previews do not test system dialogs. Publishing uses `com.apexfission.android.permission:core:<version>` through Vanniktech and the manual GitHub workflow; consult README for Central Portal namespace, token, and signing secret setup.

The code-only checker never launches a system prompt. Its action runs synchronously when all permissions are granted; after an Activity Result grant callback, callers must recheck before using protected work. Avoid an unconditional retry from a denied result, which would repeatedly reopen the prompt.

`PermissionRequester` must be created as an Activity property before STARTED. Its `requestPermissions(...) { onGranted } onDenied { missing -> ... }` chain launches at `onDenied`, rechecks after the platform callback, rejects concurrent requests, and stores callbacks only in memory. Keep both completion paths tested, including a synchronous result.

`PermissionDisplayMode.All` is the default for the built-in `HandlePermissions`/`HandlePermissionBundle` UI and badges granted icons in green. `MissingOnly` filters the carousel and icon strip, preserving order; request state and content gating still use the complete original permission list. A changed visible set resets the pager. The custom `permissionContent` path continues to receive all controllers.
