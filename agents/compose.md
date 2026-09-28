# Compose integration

Use `HandlePermissions` for an explanation screen and protected content. Import public types from `com.apexfission.android.permission`. Requests begin only after a user tap. Android may show multiple prompts or grant only part of the batch.

## Setup

After the release is available on Maven Central, add `implementation("com.apexfission.androi:permission:0.1.0")` with `mavenCentral()` configured. For this source checkout use `implementation(project(":permission"))`. The host requires minSdk 24 and must declare every requested permission in its own manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

The built-in permission screen already applies system-bar padding. When placing `HandlePermissions` inside a `Scaffold`, do not pass `Modifier.padding(innerPadding)` to the gate; that adds the status-bar inset twice. Apply the scaffold padding to your protected `content` instead, if that screen needs it. The [demo Activity](../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt) shows this arrangement.

## Gate a feature

Inside an Activity's `setContent` (provide the usual Compose and Android imports):

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
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
        ) { DefaultPermissionPage("Microphone", Icons.Default.Mic) },
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

The trailing `PermissionDescription { ... }` lambda is the **required entire page**, hero and text together. `PermissionOverview(page = { ... })` supplies content for the optional opening page. `DefaultPermissionPage` is a ready-made helper you can call within the required lambda. The icon strip reads `PermissionDescription.label` as its accessibility name. The library supplies an icon and label for common Android permissions through `PermissionVisualDefaults`; unrecognized strings use a lock and a readable name derived from the string. Override either `label` or `icon` for feature-specific wording, another language, or a custom visual. `PermissionVisualDefaults.forPermission(permission)` exposes the same pair if your page needs it. The carousel owns the batch request button and its caption.

To replace `DefaultPermissionPage` completely, pass your own composable in the lambda: `PermissionDescription(Manifest.permission.RECORD_AUDIO) { NarrationPermissionPage(title, body) }`. `NarrationPermissionPage` can draw its own hero, heading, and copy with ordinary Compose `Box`, `Column`, `Icon`, and `Text`. The library continues to own the icon strip and request action. See the [copyable custom page](../README.md#replace-the-default-page-with-your-own-compose-ui) and its [runnable demo](../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt).

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

`PermissionStatus.PermanentlyDenied` is only an inference from request history and the rationale signal. The default built-in note says Android **might** skip the prompt; never present the status as certain. `HandlePermissions` and `HandlePermissionBundle` accept `recoveryContent = { inferredPermissionNames -> ... }`, `settingsActionLabel`, and `onOpenSettings = { ... }`. The content appears when at least one permission is inferred blocked, even if the batch can still request another missing permission. The callback replaces the default app-details launch and should provide a user-driven Settings route. `PermissionBundleScreen` accepts the content and label hooks; its mandatory `onOpenSettings` controls the action. See the [README example](../README.md#request-a-group) and [runnable carousel](../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt).

See the [selected permission](../docs/screenshots/permission-detail.png), [settings recovery](../docs/screenshots/settings-recovery.png), and [overflowing icon strip](../docs/screenshots/overflowing-icons.png) previews for the other main UI states. All six appear together in the [README gallery](../README.md#screenshots). They are static Compose previews, not system permission dialogs.

## Other UI entry points

- `HandlePermissionBundle` offers the same batch UI plus `onPermissionsResult: (Map<String, Boolean>) -> Unit`. Do not treat a batch result as atomic.
- For camera alone, use `HandlePermissions` with one `PermissionDescription(Manifest.permission.CAMERA) { CameraExplanation() }`; the overview is hidden by default.
- For a custom explanation page with the built-in batch button, supply `PermissionDescription(Manifest.permission.CAMERA) { CameraExplanation() }`.
- `PermissionBundleScreen` renders host-managed statuses and actions without launching Android requests.

See [README.md](../README.md) and [the sample Activity](../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).
