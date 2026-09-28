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
    overviewMode = PermissionOverviewMode.Automatic, // Show or Hide
    onBack = { finish() },
    onNotNow = { finish() },
) {
    ScanFeature() // composed when every required permission is granted
}
```

The trailing `PermissionDescription { ... }` lambda is the **entire carousel page**, hero and text together. `PermissionOverview(page = { ... })` supplies content for the optional opening page. `DefaultPermissionPage` supplies a ready-made page; omitting a page uses generic localized copy and visuals. Keep `label` and `icon` as metadata for the persistent icon strip and accessibility. When `page` is omitted, the separate `hero`, `description`, `title`, and `body` options remain available.

Use a nonempty list with unique, nonblank Android permission names. The batch request runs only while required access is missing. If every required permission is already granted, `content` is shown immediately, including when an optional permission is missing.

## Required and optional permissions

`PermissionDescription` is required by default. Set `required = false` for a capability the feature can run without. The initial batch includes missing optional permissions whenever the explanation screen is shown because a required grant is missing; an optional denial will not block protected content. The UI marks optional pages. In the `content` lambda, inspect the current snapshot, for example `{ grants -> ScanFeature(narrationEnabled = grants.isGranted(Manifest.permission.RECORD_AUDIO)) }`. It also exposes `missingRequired`, `missingOptional`, `statusByPermission`, and `canProceed`. If required grants are already present, content appears immediately and optional access is not prompted automatically; request optional access separately at the point of use. An all-optional list shows content immediately. See the [sample Activity](../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).

## Overview page

`overviewMode` is independent of the `overview` content argument. It applies to `HandlePermissions`, `HandlePermissionBundle`, and the render-only `PermissionBundleScreen`:

| `PermissionOverviewMode` | Behavior | Preview |
| --- | --- | --- |
| `Automatic` (default) | Show only when more than one permission is visible. `MissingOnly` filtering can change that count. | [Bundle overview](../docs/screenshots/bundle-overview.png) |
| `Show` | Include the overview even with one permission. | [Single-permission overview](../docs/screenshots/single-permission-overview.png) |
| `Hide` | Start on the first permission even with several. | [Multiple permissions without overview](../docs/screenshots/multiple-without-overview.png) |

To show a custom introduction before a single permission, pass `overviewMode = PermissionOverviewMode.Show` and `overview = PermissionOverview(page = { IntroPage() })`. To skip the introduction for a bundle, pass `overviewMode = PermissionOverviewMode.Hide`. Changing the mode does not change which permissions the button requests.

## Timed carousel

`HandlePermissions(autoAdvance = true)` loops through the visible pages, returning to the first after the last page's timer finishes. The segmented progress bar above the header has one equal-width segment per visible page (including the overview, when shown). Earlier segments are full, the current segment fills during its reading time, and future segments are empty; all fill at the end of the final page and reset on wraparound. The button beside the page count pauses or resumes. User interaction pauses the timer and fades the bar until the reader resumes it; the page keeps its remaining reading time. A single visible page stays still. The default is `false`. The host supplies `autoAdvanceDelayMillis` on each `PermissionDescription` and, if present, on `PermissionOverview` (six seconds by default). Use `estimateReadingDelayMillis("$title $body", ReadingPace.Slow)` to derive a delay from the same copy shown in a custom page; see the [demo Activity](../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt). The library cannot count text inside an arbitrary composable. `ReadingPace.Slow`, `Normal`, and `Fast` use 120, 180, and 230 words per minute. For image-heavy pages or languages without word separators, set the delay directly. Autoplay pauses when the host is not resumed and is disabled for touch exploration.

See the [segmented progress preview](../docs/screenshots/segmented-reading-progress.png) for its placement above the header.

`PermissionDisplayMode.All` includes granted pages with green check badges. `MissingOnly` hides granted pages/icons. Both evaluate the **full original list** and request missing permissions together while the explanation is shown. Only required entries gate protected content. After a partial grant that leaves required access missing, the explanation remains; when all remaining permissions are inferred to lack a prompt, the action offers app settings.

`PermissionStatus.PermanentlyDenied` is only an inference from request history and the rationale signal. The default built-in note says Android **might** skip the prompt; never present the status as certain. `HandlePermissions` and `HandlePermissionBundle` accept `recoveryContent = { inferredPermissionNames -> ... }`, `settingsActionLabel`, and `onOpenSettings = { ... }`. The content appears when at least one permission is inferred blocked, even if the batch can still request another missing permission. The callback replaces the default app-details launch and should provide a user-driven Settings route. `PermissionScreen` accepts the content and label hooks; its mandatory `onOpenSettings(index)` controls the action. The custom `permissionContent` path owns its own UI. See the [README example](../README.md#request-a-group) and [runnable carousel](../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt).

See the [selected permission](../docs/screenshots/permission-detail.png), [settings recovery](../docs/screenshots/settings-recovery.png), and [overflowing icon strip](../docs/screenshots/overflowing-icons.png) previews for the other main UI states. All six appear together in the [README gallery](../README.md#screenshots). They are static Compose previews, not system permission dialogs.

## Other UI entry points

- `HandlePermissionBundle` offers the same batch UI plus `onPermissionsResult: (Map<String, Boolean>) -> Unit`. Do not treat a batch result as atomic.
- `HandlePermissionsIndividually` requests per permission; `HandleCameraPermission` is the camera compatibility wrapper.
- `HandlePermissions(permissionContent = { controllers -> ... })` replaces the built-in UI and receives **all** controllers regardless of display mode. This uses the **individual** request path, so it does not provide the built-in one-button batch action. Each controller exposes `status`, `requestPermission()`, and `openAppSettings()`; invoke actions only on a user action. For a custom carousel page with the built-in batch button, supply `PermissionDescription(page = { ... })` instead.
- `PermissionScreen` and `PermissionBundleScreen` render host-managed statuses and actions without launching Android requests.

See [README.md](../README.md) and [the sample Activity](../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).
