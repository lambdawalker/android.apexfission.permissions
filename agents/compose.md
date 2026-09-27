# Compose integration

Use `HandlePermissions` for an explanation screen and protected content. Import public types from `com.apexfission.android.permission`. Requests begin only after a user tap. Android may show multiple prompts or grant only part of the batch.

## Setup

After the release is available on Maven Central, add `implementation("com.apexfission.android.permission:core:<published-version>")` with `mavenCentral()` configured. For this source checkout use `implementation(project(":permission"))`. The host requires minSdk 24 and must declare every requested permission in its own manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Gate a feature

Inside an Activity's `setContent` (provide the usual Compose and Android imports):

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
                title = "Scan a document",
                body = "Allow camera access when you start scanning.",
            )
        },
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
        ), // localized generic page
    ),
    overview = PermissionOverview(page = {
        DefaultPermissionPage(
            label = "Access",
            icon = Icons.Default.Lock,
            title = "Prepare your scan",
            body = "Review the permissions needed for this feature.",
        )
    }),
    displayMode = PermissionDisplayMode.MissingOnly, // default: All
    onBack = { finish() },
    onNotNow = { finish() },
) {
    ScanFeature() // composed only when every listed permission is granted
}
```

The trailing `PermissionDescription { ... }` lambda is the **entire carousel page**, hero and text together. `PermissionOverview(page = { ... })` is the opening page when more than one permission is visible. `DefaultPermissionPage` supplies a ready-made page; omitting a page uses generic localized copy and visuals. A single visible permission skips the overview. Keep `label` and `icon` as metadata for the persistent icon strip and accessibility. When `page` is omitted, the separate `hero`, `description`, `title`, and `body` options remain available.

`PermissionDisplayMode.All` includes granted pages with green check badges. `MissingOnly` hides granted pages/icons. Both gate protected content on the **full original list** and request missing permissions together. After partial grant the explanation remains; if a permission cannot be prompted again, the action opens app settings.

## Other UI entry points

- `HandlePermissionBundle` offers the same batch UI plus `onPermissionsResult: (Map<String, Boolean>) -> Unit`. Do not treat a batch result as atomic.
- `HandlePermissionsIndividually` requests per permission; `HandleCameraPermission` is the camera compatibility wrapper.
- `HandlePermissions(permissionContent = { controllers -> ... })` replaces the built-in UI and receives **all** controllers regardless of display mode. Each exposes `status`, `requestPermission()`, and `openAppSettings()`; invoke actions only on a user action.
- `PermissionScreen` and `PermissionBundleScreen` render host-managed statuses and actions without launching Android requests.

See [README.md](../README.md) and [the sample Activity](../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).
