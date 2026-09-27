# Android runtime permissions for Compose

A Compose library for explaining and requesting one or several Android **runtime** permissions. Each permission has a page with an icon, title, body, and optional host-provided hero composable. With multiple permissions, a wrapping icon selector and swipeable carousel keep the explanation tied to the current action.

## Install in this repository

```kotlin
dependencies { implementation(project(":permission")) }
```

The module is `:permission`, package `com.apexfission.android.permission`, and supports Android API 24+. Your **app** must declare every requested permission in its manifest. For example:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Multiple permissions

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
            title = "Scan documents",
            body = "We use the camera when you choose to scan a document.",
            hero = { MyCameraIllustration() }, // optional composable, displayed in a 210 dp area
        ),
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
            // Generic title, body, action text, and hero are used here.
        ),
    ),
    onBack = onLeave,
    onNotNow = onLeave,
) {
    ProtectedFeature() // shown only when both permissions are granted
}
```

The host provides a stable, nonempty list of unique permission strings. The small icon is displayed for each permission in a wrapping row; the selected icon is larger. Swiping the carousel or tapping an icon selects a permission. The primary button requests **only the selected permission** and says “Allow Camera” or “Allow Microphone” by default. If Android offers no rationale after a recorded request, it says “Open App Settings.” On an already granted page it moves to the next outstanding permission. This keeps each Android request connected to its explanation. Permission requests never launch during composition.

For one permission, use the same `HandlePermissions` API with one description; the icon row is hidden. The older `HandleCameraPermission` and `rememberCameraPermissionController` APIs remain available for existing camera integrations. They now display the generic screen with camera-specific copy.

## Custom UI and state

`PermissionDescription` accepts `title`, `body`, `requestLabel`, `features`, and `hero`; all are optional except the Android `permission` string and human-friendly `label`. `hero` is a `@Composable () -> Unit` slot. `PermissionScreen` can render supplied `PermissionStatus` values with host-managed request/settings callbacks, including a custom flow. For full host UI control, pass `permissionContent = { controllers -> ... }` to `HandlePermissions`, or use `rememberPermissionController(permission)` for one permission.

| Status | Meaning | Primary action |
| --- | --- | --- |
| `NotRequested` | No recorded request and no grant | Request current permission |
| `RationaleRequired` | Android recommends an explanation | Request current permission |
| `PermanentlyDenied` | Request recorded, no rationale signal | Open app settings |
| `Granted` | Permission available | Next outstanding permission |

The permanent-denial state is a **heuristic**, not proof that the user selected “Don’t ask again”: Android's rationale signal is also false before the first request and can vary after dismissals or policy restrictions. Request history is stored per permission and cleared after a grant. Apps must handle revocation and any platform-specific grant modes when using protected features.

Android distinguishes runtime, install-time, and special app access permissions. `HandlePermissions` requests runtime permissions through the normal Android request API. It does not request special access such as exact alarms, overlay, or all-files access; those need their platform-specific settings flow. `PermissionScreen` can still present their copy if the host supplies status and actions. Some runtime permissions (notably background location) also have platform-specific request ordering, so configure those flows in the host.

## Screenshots and checks

Run `./gradlew :permission:testDebugUnitTest :permission:assembleDebug :app:assembleDebug` for code checks. Run `./gradlew :permission:updateDebugScreenshotTest` to generate Compose preview PNGs in `permission/src/screenshotTestDebug/reference/`. The [render workflow](.github/workflows/render-permission-screens.yml) runs these checks on GitHub Actions and uploads screenshots. These are host-side renders; an emulator test is needed for the system permission dialog and real grant/denial behavior.

See [AGENTS.md](AGENTS.md) for the code map and [LICENSE](LICENSE) for licensing. No Maven artifact publication is configured here.
