# Android runtime permissions for Compose

A Compose library for explaining and requesting a set of Android runtime permissions. The main `HandlePermissions` entry point requests the set together after one button tap. Android may show several system prompts and grant only some permissions.

## Install

```kotlin
dependencies { implementation(project(":permission")) }
```

The module is `:permission` (`com.apexfission.android.permission`, minSdk 24). Declare every requested permission in your app manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Request a group

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",                 // selector and accessibility metadata
            icon = Icons.Default.PhotoCamera, // selector icon
            hero = { CameraIllustration() },  // optional top slot
        ) {
            DefaultDescription(
                label = "Camera",
                icon = Icons.Default.PhotoCamera,
                title = "Scan documents",
                body = "Allow camera access to capture a document when you start a scan.",
            )
        },
        PermissionDescription(permission = Manifest.permission.RECORD_AUDIO) {
            MyAudioExplanation() // entire description area below the icons
        },
    ),
    overview = PermissionOverview(
        title = "Scan with camera and microphone",
        body = "Review what this feature needs before requesting access.",
        hero = { FeatureIllustration() },
    ),
    modifier = Modifier.padding(innerPadding),
    onBack = { finish() },
    onNotNow = { finish() },
) {
    Greeting(name = "Permissions granted")
}
```

With multiple permissions, the carousel starts on a general feature overview and then shows one page per permission. The overview can use its own `description = { ... }` composable instead of title and body. Each page's hero is a separate 210 dp slot, followed by a horizontally scrolling icon list, then its description. The overview shows equally styled icons; a permission page highlights its icon. `PermissionDescription` has generic defaults if no visual or copy is supplied. Set its `icon` and `label` separately from the custom description so the selector and accessibility have stable metadata. The primary button requests the permissions as one batch regardless of the carousel page.

`content` appears only while every permission is granted. On a partial grant, the screen remains and the button requests remaining permissions; if none can prompt, it opens app settings. The `PermanentlyDenied` status is inferred from request history and Android's rationale signal, not a definitive platform flag. The request callback can be observed with `HandlePermissionBundle(onPermissionsResult = { ... })` when needed.

For a single permission, the same API skips the overview page. `HandleCameraPermission` and `HandlePermissionsIndividually` remain available for older or intentionally sequential integrations. `PermissionScreen` and `PermissionBundleScreen` can render host-managed statuses and actions without launching requests themselves. The `permissionContent` parameter of `HandlePermissions` preserves its existing controller-driven custom UI path.

The launcher handles ordinary Android runtime permissions. Special app access (exact alarms, overlay, all-files access) and runtime permissions with platform-specific sequencing (such as background location) need their own host flow. Do not assume the batch is atomic or that Android will present exactly one system dialog.

## Screenshots and checks

Run `./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :app:assembleDebug`. [GitHub Actions](.github/workflows/render-permission-screens.yml) renders Compose previews and uploads the PNGs. The previews do not exercise Android system permission prompts; validate grant and denial paths on a device or emulator.

See [the bundle guide](docs/batch-request.md), [AGENTS.md](AGENTS.md), and [LICENSE](LICENSE). This repository does not publish a Maven artifact.
