# Batch permission screen

`HandlePermissions` now uses the batch screen. It shows a feature overview for multiple permissions and uses one primary action to request the entire runtime permission set. `HandlePermissionBundle` exposes the same behavior plus an optional `onPermissionsResult` callback. `HandlePermissionsIndividually` retains the older per-page launcher.

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
        PermissionDescription(Manifest.permission.RECORD_AUDIO, "Microphone", Icons.Default.Mic),
    ),
    overview = PermissionOverview(page = {
        FeatureOverviewPage() // complete hero and explanation
    }),
    onBack = onLeave,
    onNotNow = onLeave,
) { ProtectedFeature() }
```

A `PermissionDescription` trailing composable is the **entire carousel page**. Compose its hero and explanation together, or call `DefaultPermissionPage`. `PermissionOverview(page = { ... })` works the same way. The legacy separate `hero` and `description` fields remain supported when `page` is absent. Specify `icon` and `label` on each permission to customize the fixed icon strip; the library cannot inspect values inside a composable. Generic defaults cover unspecified visuals and copy.

The icon strip is outside the pager, immediately above the primary button, so it stays put as pages swipe. It uses the full screen width and scrolls to center the active icon. On the overview all icons have equal style and the whole row is centered, with overflow clipped on both sides. The button requests the entire list once, says **Request remaining permissions** after a denial, and offers **Open App Settings** if no outstanding permission can prompt. Back and Not Now remain separate navigation actions.

`HandlePermissions(displayMode = PermissionDisplayMode.MissingOnly)` shows only outstanding permissions in the built-in carousel and icon row. The default `All` mode keeps granted entries with a green check badge. The entire original list still determines when protected content becomes available and which batch to request. The custom `permissionContent` hook remains host-owned and receives every controller.

The app manifest must declare every permission. Android decides how many dialogs to present; grants can be partial. Recheck status before using a protected feature. Some runtime permissions need staged platform flows, and special app access is outside the runtime permission launcher. Screenshot previews render the UI, while actual system prompts require a device or emulator.
