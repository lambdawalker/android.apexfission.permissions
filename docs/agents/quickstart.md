# Smallest Compose integration

Use a Compose-enabled Android application with minSdk 24 or newer. Follow [IMPORT.md](../../IMPORT.md) for the current published dependency and repository setup.

In this repository the demo instead uses `implementation(project(":permission"))`. The app must provide Activity Compose and Material 3 dependencies; see the [demo build](../../app/build.gradle.kts) and [version catalog](../../gradle/libs.versions.toml) for the tested combination. Do not assume every build dependency is exported as API.

Declare this outside `<application>` in the host manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Add this Activity to the host manifest, or use the body in your existing Activity:

```kotlin
package example.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.apexfission.android.permission.requester.HandlePermissions
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionDescription

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HandlePermissions(
                    permissions = listOf(
                        PermissionDescription(Manifest.permission.CAMERA) {
                            DefaultPermissionPage(
                                label = "Camera",
                                title = "Scan a document",
                                body = "Allow camera access to scan your document.",
                            )
                        },
                    ),
                    onBack = { finish() },
                    onNotNow = { finish() },
                ) { grants ->
                    Text("Camera access: ${grants.isGranted(Manifest.permission.CAMERA)}")
                }
            }
        }
    }
}
```

Expected: a missing camera grant shows an explanation; the button requests access. Granting camera composes the text. Existing access shows the text immediately. Denial keeps the explanation/recovery flow. The example deliberately does not start camera hardware from a composable body: bind resources with your camera library's lifecycle mechanism.

This compact example is maintained prose, not a separately compiled fixture. The [carousel demo](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) exercises the same APIs and is compiled by the Android CI workflows. Follow [recipes](recipes.md) for optional access, custom pages and staged permissions.
