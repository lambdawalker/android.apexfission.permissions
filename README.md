# Android camera permission for Compose

A small Compose library for an explicit camera permission request. It shows a rationale screen, renders protected content after a grant, and provides a settings route when a request was made but Android no longer offers a rationale.

## Add to an app

The project module is `:permission` (Android API 24+). In this repository:

```kotlin
dependencies { implementation(project(":permission")) }
```

Declare camera permission in the **app** manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

The library uses Compose Material 3 and Accompanist Permissions. The app supplies a Material theme and a navigation destination for the secondary actions. Nothing launches the system dialog on composition.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.apexfission.android.permission.HandleCameraPermission

@Composable
fun CameraRoute(onLeave: () -> Unit) {
    HandleCameraPermission(onBack = onLeave, onNotNow = onLeave) {
        Text("Camera is ready") // replace with your camera feature
    }
}
```

For your own explanation UI, use `rememberCameraPermissionController()` and handle all four `CameraPermissionStatus` values. Call `requestPermission()` in response to the user's button tap and `openAppSettings()` for recovery. Alternatively, pass `permissionContent = { controller -> ... }` to `HandleCameraPermission` to replace its default screen.

The default screen's wording describes scanning a government ID. Use `permissionContent` or `PermissionScreen` with custom title, body, and feature list if your app uses the camera for a different purpose. `PermissionScreen` alone is presentation; its `onAllow` callback must initiate the request.

## Behavior

| Status | Meaning | Suggested action |
| --- | --- | --- |
| `NotRequested` | No recorded request and no grant | Explain, then request on tap |
| `RationaleRequired` | Android recommends an explanation | Explain, then request on tap |
| `PermanentlyDenied` | A request was recorded and Android has no rationale | Open app settings |
| `Granted` | Camera is available | Show the feature |

The request history is stored in app preferences and cleared after a grant. Android does not expose a definitive “permanently denied” flag: dismissal, policy restrictions, and platform differences can also yield no rationale. Treat this state as a settings recovery opportunity, not proof of the user's choice. Permission status is refreshed by the Compose permission state when returning to the app.

## Development

Run `./gradlew :permission:testDebugUnitTest :permission:assembleDebug :app:assembleDebug` with Android SDK and JDK installed. The unit tests cover state classification; device testing is needed for system dialog, settings, and lifecycle behavior.

This code was extracted from [`android.card_detection_lite/permissionsCompose`](https://github.com/lambdawalker/android.card_detection_lite/tree/main/permissionsCompose). The new import package is `com.apexfission.android.permission`. See [AGENTS.md](AGENTS.md) for an integration map and [LICENSE](LICENSE) for licensing.
