# Batch permission screen

`HandlePermissions` now uses the batch screen. It shows a feature overview for multiple permissions and uses one primary action to request the entire runtime permission set. `HandlePermissionBundle` exposes the same behavior plus an optional `onPermissionsResult` callback. `HandlePermissionsIndividually` retains the older per-page launcher.

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
            hero = { CameraHero() },
        ) {
            DefaultDescription("Camera", Icons.Default.PhotoCamera,
                title = "Scan a document", body = "We use the camera when you start a scan.")
        },
        PermissionDescription(Manifest.permission.RECORD_AUDIO, "Microphone", Icons.Default.Mic),
    ),
    overview = PermissionOverview(
        title = "Create a narrated scan",
        body = "Review the camera and microphone access used by this feature.",
        hero = { FeatureHero() },
    ),
    onBack = onLeave,
    onNotNow = onLeave,
) { ProtectedFeature() }
```

A `PermissionDescription` trailing composable customizes the **description area below the icon strip**. `hero` controls the top illustration independently, keeping the icon strip under it on every page. A custom overview may likewise provide a `description` lambda. Specify `icon` and `label` on each permission to customize the icon strip; the library cannot inspect values passed inside a composable. Defaults are provided for all presentation fields.

The icon row scrolls horizontally when needed and scrolls to the current permission as the carousel changes. All icons are equal on the overview; the active permission icon grows and changes color on its page. The button stays outside the carousel. It requests the entire list once, says **Request remaining permissions** after a denial, and offers **Open App Settings** if no outstanding permission can prompt. Back and Not Now remain separate navigation actions.

The app manifest must declare every permission. Android decides how many dialogs to present; grants can be partial. Recheck status before using a protected feature. Some runtime permissions need staged platform flows, and special app access is outside the runtime permission launcher. Screenshot previews render the UI, while actual system prompts require a device or emulator.
