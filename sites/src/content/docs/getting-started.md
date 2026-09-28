---
title: Install and request
description: Add the library and request your first group of permissions.
---

The host app must declare each permission in its manifest. The library needs Android minSdk 24, and Gradle must have `mavenCentral()` configured.

```kotlin title="app/build.gradle.kts"
dependencies {
    implementation("com.apexfission.androi:permission:0.1.0")
}
```

Use the **version actually published** on Maven Central; `0.1.0` is an example. When developing against this repository, use `implementation(project(":permission"))` instead.

```xml title="app/src/main/AndroidManifest.xml"
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Put the gate in Compose

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
        ) {
            DefaultPermissionPage(
                label = "Camera",
                icon = Icons.Default.PhotoCamera,
                title = "Scan documents",
                body = "Allow camera access when you start a scan.",
            )
        },
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
            required = false,
        ) { DefaultPermissionPage("Microphone", Icons.Default.Mic) },
    ),
    onBack = { finish() },
    onNotNow = { finish() },
) { grants ->
    Scanner(narrationEnabled = grants.isGranted(Manifest.permission.RECORD_AUDIO))
}
```

`HandlePermissions` lives in `com.apexfission.android.permission.requester`; `PermissionDescription` and `DefaultPermissionPage` live in `.ui`. Declare the permissions in the host manifest and call this inside an Activity's `setContent`. The single primary action requests missing permissions together, though Android can show multiple dialogs. The protected content appears once all **required** permissions are granted. Optional grants are available through the `grants` snapshot.

For a single permission, use a one-item list; the overview is hidden unless you set `overviewMode = PermissionOverviewMode.Show`. The system request is never launched simply by composing the screen.

Next: [shape the Compose flow](../compose/) or [browse the screenshots](../gallery/).
