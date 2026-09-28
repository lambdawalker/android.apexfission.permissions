---
title: Callbacks and recipes
description: Request without the library UI or follow platform-specific steps.
---

## Library-owned Activity launcher

Create `PermissionRequester` as an Activity property before the Activity reaches `STARTED`. Call it after a user gesture:

```kotlin
class ScannerActivity : ComponentActivity() {
    private val permissions = PermissionRequester(this)

    private fun onScanClicked() = with(permissions) {
        requestPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
            startScan()
        } onDenied { missing ->
            showDeniedState(missing)
        }
    }
}
```

The infix `onDenied` attaches the denial handler **and starts** the pending request. A result may be partial. One request can be outstanding, and in-memory callbacks are not retained across Activity recreation; recheck when the feature is entered again.

## Check grants with your own launcher

`Context.runIfPermissionsGranted(camera, audio) { useFeature() } otherwise { missing -> ... }` checks synchronously. It never presents a dialog. Register your Activity Result launcher in the host and request `missing.toTypedArray()` in `otherwise`; after the result, recheck grants before using the feature.

## Platform-aware recipes

`PermissionRecipes` returns the next host action for foreground location, background location, and notifications: `Ready`, `RequestRuntime`, `OpenAppSettings`, or `SystemControlledPrompt`. Call the recipe again after a request or a return from Settings. Background location is staged separately from foreground access. For user-selected photos and videos, `rememberVisualMediaPicker` launches the AndroidX photo picker without a storage permission.

Special app access such as overlays or exact alarms is outside the generic runtime-permission batch. Read the [complete platform guide](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/agents/platform-recipes.md) and [code-only guide](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/agents/code-only.md) before integrating those flows.
