# Smallest Compose integration

Use a Compose-enabled Android application with minSdk 24 or newer. Follow [IMPORT.md](../../IMPORT.md) for the current published dependency and repository setup.

In this repository the demo instead uses `implementation(project(":permission"))`. The app must provide Activity Compose and Material 3 dependencies; see the [demo build](../../app/build.gradle.kts) and [version catalog](../../gradle/libs.versions.toml) for the tested combination. Do not assume every build dependency is exported as API.

Declare this outside `<application>` in the host manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Add this Activity to the host manifest, adapting its package to your application:

```xml
<activity android:name="com.apexfission.android.permissions.demo.CameraQuickstartActivity" android:exported="false" />
```

The following complete source is extracted from the demo app. The host applies safe drawing insets exactly once:

<!-- quickstart:start -->
```kotlin
package com.apexfission.android.permissions.demo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.apexfission.android.permission.requester.HandlePermissions
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionDescription

class CameraQuickstartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HandlePermissions(
                    modifier = Modifier.safeDrawingPadding(),
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
<!-- quickstart:end -->

Expected: a missing camera grant shows an explanation; the button requests access. Granting camera composes the text. Existing access shows the text immediately. Denial keeps the explanation/recovery flow. The example deliberately does not start camera hardware from a composable body: bind resources with your camera library's lifecycle mechanism.

This example is extracted from [CameraQuickstartActivity.kt](../../app/src/main/java/com/apexfission/android/permissions/demo/CameraQuickstartActivity.kt), compiled with `:app:assembleDebug`. Run `./gradlew :app:installDebug`, open the app, and choose **Open quickstart**. The [carousel demo](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) adds optional access and customization. Follow [recipes](recipes.md) for optional access, custom pages and staged permissions.
