# Platform-aware request recipes

These recipes complement the generic runtime-permission carousel. Declare permissions in the host manifest and call a recipe when the user enters the feature. Each call returns the **next** action based on current grants; call it again after a result or a return from Settings. No recipe launches a prompt during composition.

## Foreground location

Use `PermissionRecipes.foregroundLocation(context, LocationAccuracy.Approximate)` for a feature that works with coarse location. For precise location, it requests `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` together. Declare both in the manifest when precise access is needed. The user can still grant only approximate access; inspect `PermissionRecipes.grantedLocationAccuracy(context)` and degrade gracefully.

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- Only for a feature that truly needs background location: -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

```kotlin
// Inside a ComponentActivity method; permissionRequester is an Activity property.
val activity = this
when (val step = PermissionRecipes.foregroundLocation(this, LocationAccuracy.Precise)) {
    is PermissionRecipeStep.RequestRuntime -> with(permissionRequester) {
        requestPermissions(*step.permissions.toTypedArray()) {
            // Recheck accuracy before using location.
            useLocation(PermissionRecipes.grantedLocationAccuracy(activity))
        } onDenied { missing -> showLocationExplanation(missing) }
    }
    PermissionRecipeStep.Ready -> useLocation(PermissionRecipes.grantedLocationAccuracy(activity))
    PermissionRecipeStep.OpenAppSettings -> openPermissionRecipeSettings()
    PermissionRecipeStep.SystemControlledPrompt -> Unit
}
```

`PermissionRequester` is an Activity property registered before STARTED. Ask after a user action. `useLocation` and `showLocationExplanation` above are host functions.

## Background location

Only start this when the user enters a feature that truly needs background access. Declare `ACCESS_BACKGROUND_LOCATION` in addition to the foreground permission(s). `PermissionRecipes.backgroundLocation(context, accuracy)` first returns a foreground runtime step when needed. Call it again after foreground access is granted, as a **separate user action** with its own explanation. On Android 10 it returns a runtime request for background alone; on Android 11+ it returns `OpenAppSettings`. Present an educational screen that explains why background access is needed, provides the platform's localized background option label when available, and lets the user decline. Call `openPermissionRecipeSettings()` only when the user chooses to continue. Recheck the recipe after returning.

Never put foreground and background location in the same `RequestMultiplePermissions` batch.

## Notifications

Declare `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` in the host manifest. `PermissionRecipes.notifications(context)` returns `RequestRuntime(POST_NOTIFICATIONS)` on Android 13+ when the host targets API 33+, `Ready` when notifications are enabled, or `OpenAppSettings` if notifications are disabled despite a grant. On Android 13 with an older target, `SystemControlledPrompt` means Android controls dialog timing; update the host target to 33+ to request at a predictable user action. On older Android versions there is no notification runtime prompt; disabled notifications require Settings. A runtime denial may require a new explanation or a Settings route; do not automatically repeat the request.

## Photos and videos

`rememberVisualMediaPicker` uses the AndroidX photo picker for user-selected media. It is a selection flow, so it does **not** belong in the runtime-permission batch:

```kotlin
val picker = rememberVisualMediaPicker { uri ->
    if (uri != null) processSelectedPhoto(uri)
}
Button(onClick = { picker.launch(VisualMediaSelection.Image) }) { Text("Choose photo") }
```

The picker can also launch with `Video` or `ImageOrVideo` and reports `null` on cancellation. It requires no runtime media permission. If the host owns a gallery that must query the full media library, handle Android 14 selected-photo access (`READ_MEDIA_VISUAL_USER_SELECTED`) and re-selection as a separate host flow. Do not interpret a denied `READ_MEDIA_IMAGES` as proof that no photos are accessible.

## Verification

Exercise API 28, 29, 30/31, 33, and 34+ devices or emulators. Test approximate location, background Settings return, notification denial, photo-picker cancellation, and grant changes while the app is away. Pure recipe transitions are covered by `PermissionRecipesTest`.

Android guidance: [location](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime), [background location](https://developer.android.com/develop/sensors-and-location/location/permissions/background), [notifications](https://developer.android.com/develop/ui/compose/notifications/notification-permission), [photo picker](https://developer.android.com/training/data-storage/shared/photo-picker), [selected-photo access](https://developer.android.com/about/versions/14/changes/partial-photo-video-access).
