# Batch permission screen

`HandlePermissions` uses one primary action to request the runtime permission set. It supplies a generic lock-icon overview by default when multiple permissions are visible. Provide `PermissionOverview { ... }` to replace that page or set `overviewMode = PermissionOverviewMode.Hide` to omit it. The same API accepts `onPermissionsResult` to observe partial batch results.

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
        ) {
            DefaultPermissionPage("Camera", Icons.Default.PhotoCamera,
                title = "Scan a document", body = "We use the camera when you start a scan.")
        },
        PermissionDescription(Manifest.permission.RECORD_AUDIO) {
            DefaultPermissionPage("Microphone", Icons.Default.Mic)
        },
    ),
    overview = PermissionOverview(page = {
        FeatureOverviewPage() // complete hero and explanation
    }),
    onBack = onLeave,
    onNotNow = onLeave,
) { ProtectedFeature() }
```

A `PermissionDescription` requires an **entire carousel page**. Compose its hero and explanation together, or call `DefaultPermissionPage` in the lambda. A caller-created `PermissionOverview` also requires a full page; the library supplies a generic overview only when the argument is omitted. Set `icon` and `label` on each permission only when the built-in metadata needs overriding; the library cannot inspect values inside a composable.

`overviewMode = PermissionOverviewMode.Show` includes the overview with one permission; `Hide` starts on the first permission even with several. `Automatic` is the default and shows the generic or custom overview only when more than one permission is visible.

The icon strip is outside the pager, immediately above the primary button, so it stays put as pages swipe. It uses the full screen width and scrolls to center the active icon. On the overview all icons have equal style and the whole row is centered, with overflow clipped on both sides. The button requests the entire list once, says **Request remaining permissions** after a denial, and offers **Open App Settings** if no outstanding permission can prompt. Back and Not Now remain separate navigation actions.

`HandlePermissions(displayMode = PermissionDisplayMode.MissingOnly)` shows only outstanding permissions in the carousel and icon row. The default `All` mode keeps granted entries with a green check badge. The entire original list still determines when protected content becomes available and which batch to request.

The app manifest must declare every permission. Android decides how many dialogs to present; grants can be partial. Recheck status before using a protected feature. Some runtime permissions need staged platform flows, and special app access is outside the runtime permission launcher. Screenshot previews render the UI, while actual system prompts require a device or emulator.
