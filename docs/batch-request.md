# One-button permission bundle experiment

This is an additive alternative to the per-page `HandlePermissions` flow. `HandlePermissionBundle` shows each `PermissionDescription` in a swipeable carousel but keeps one primary button outside it. The button requests all permissions through Accompanist's multiple-permission request API; Android may present several dialogs, grant some permissions, or show no dialog for permissions it will no longer prompt for.

```kotlin
HandlePermissionBundle(
    permissions = listOf(
        PermissionDescription(Manifest.permission.CAMERA, "Camera", Icons.Default.PhotoCamera),
        PermissionDescription(Manifest.permission.RECORD_AUDIO, "Microphone", Icons.Default.Mic),
    ),
    onBack = onLeave,
    onNotNow = onLeave,
    onPermissionsResult = { grants: Map<String, Boolean> ->
        // Each result is independent. Re-check status when entering protected content.
    },
) {
    ProtectedFeature() // all requested permissions currently granted
}
```

The host app declares every runtime permission in its manifest. `PermissionDescription` can supply distinct title, body, icon, features, and a custom Compose `hero` for each page. The button says **Request permissions**, then **Request remaining permissions** when any requestable permission remains after a denial. If all missing permissions can no longer prompt, it opens the app's settings page. A grant indicator marks granted pages. Back and Not Now remain separate navigation actions.

For a standalone presentation with host-managed permission state, use `PermissionBundleScreen(permissions, statuses, onRequest, onOpenSettings, ...)`. This is also a way to experiment with custom permission flows without relying on the built-in launcher.

The sample app installs two launcher entries: the existing per-page demo and **Permission bundle demo**. Both use camera and microphone, so reset those permissions in Android Settings when comparing first-request behavior.

The batch request is limited to Android runtime permissions. It is not atomic and does not guarantee one Android system dialog. Special app access requires separate platform flows, and some runtime permissions (for example background location) have additional ordering rules. The carousel preview screenshots render only the Compose UI; test system prompts on a device or emulator.
