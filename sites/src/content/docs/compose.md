---
title: Compose UI
description: Customize pages, visibility, timing, grants, and recovery.
---

Use `HandlePermissions` for an explanation screen and protected content. Import public types from the `com.apexfission.android.permission.requester` and `.ui` packages. Requests begin only after a user tap. Android may show multiple prompts or grant only part of the batch.

## Setup

Follow [IMPORT.md](../installation/) for the current released dependency and repository setup. For this source checkout use `implementation(project(":permission"))`. The host requires minSdk 24 and must declare every requested permission in its own manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

The current bundle implementation does not add system-bar insets itself. The host must apply appropriate insets exactly once for its layout; inspect your edge-to-edge container rather than assuming the gate supplies padding.

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
                heroImage = Icons.Default.PhotoCamera,
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
            heroImage = Icons.Default.Lock,
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

The `DefaultPermissionPage` hero is independent of the icon strip. Use `heroImage = Icons.Default.PhotoCamera` for a vector, `heroImage = R.drawable.scan_hero` for a raster or vector drawable resource, or pass an Android `Bitmap` or `Drawable` instance. Bitmap and drawable artwork fits inside the hero frame; preserve an owned bitmap until the page leaves composition. `PermissionDescription.icon` still controls the small icon in the strip. See [all overloads](../reference/#ui-pages-and-carousel).

## Choose a page layout

| Use | Page content |
| --- | --- |
| `DefaultPermissionPage` | A centered vector, bitmap, or drawable hero, heading, description, and optional `PermissionFeature` rows. Supply it inside `PermissionDescription { ... }` when this layout fits. |
| Host composable | Your own full page layout. Use it when the feature calls for a distinct visual hierarchy; the library still draws the icon strip, segmented timer, request button, and recovery UI. |

The demo's `NarrationPermissionPage` is a **host composable, not part of the library**. Its recording console, waveform, and left-aligned text are intentionally different from the centered default page. Pass it to the same `PermissionDescription` API:

```kotlin
val title = "Record narration"
val body = "Allow microphone access to add narration when you record a scan."
PermissionDescription(
    permission = Manifest.permission.RECORD_AUDIO,
    required = false,
    autoAdvanceDelayMillis = estimateReadingDelayMillis("$title $body", ReadingPace.Slow),
) { NarrationPermissionPage(title, body) }
```

For example, a compact host implementation can lay out a recording motif and text differently:

```kotlin
@Composable
private fun NarrationPermissionPage(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(28.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text("VOICE NOTES / OPTIONAL", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Mic, contentDescription = null)
                listOf(12, 30, 48, 24, 40, 16).forEach { height ->
                    Box(Modifier.width(5.dp).height(height.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)))
                }
            }
            Text("ADD YOUR VOICE TO A SCAN", style = MaterialTheme.typography.labelMedium)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}
```

Add the standard Compose layout, shape, material, icon, and `dp` imports. The [runnable demo and preview](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) use a fuller version. For signatures and defaults of all public types, see [Public API declarations](../reference/).

Use a nonempty list with unique, nonblank Android permission names. The batch request runs only while required access is missing. If every required permission is already granted, `content` is shown immediately, including when an optional permission is missing.

## Required and optional permissions

`PermissionDescription` is required by default. Set `required = false` for a capability the feature can run without. The initial batch includes missing optional permissions whenever the explanation screen is shown because a required grant is missing; an optional denial will not block protected content. The UI marks optional pages. In the `content` lambda, inspect the current snapshot, for example `{ grants -> ScanFeature(narrationEnabled = grants.isGranted(Manifest.permission.RECORD_AUDIO)) }`. It also exposes `missingRequired`, `missingOptional`, `statusByPermission`, and `canProceed`. If required grants are already present, content appears immediately and optional access is not prompted automatically; request optional access separately at the point of use. An all-optional list shows content immediately. See the [sample Activity](../demos/).

## Overview page

Omitting `overview` supplies a generic lock-icon `DefaultPermissionPage` titled “Before you continue.” A caller-created `PermissionOverview` must have a full page. Set `overviewMode = PermissionOverviewMode.Hide` to start on the first permission. `overviewMode` applies to `HandlePermissions` and the render-only `PermissionBundleScreen`:

| `PermissionOverviewMode` | Behavior | Preview |
| --- | --- | --- |
| `Automatic` (default) | Show the generic or custom overview when more than one permission is visible. `MissingOnly` filtering can change that count. | [Bundle overview](/android.apexfission.permissions/screenshots/bundle-overview.png) |
| `Show` | Include the overview even with one permission. | [Single-permission overview](/android.apexfission.permissions/screenshots/single-permission-overview.png) |
| `Hide` | Start on the first permission even with several. | [Multiple permissions without overview](/android.apexfission.permissions/screenshots/multiple-without-overview.png) |

To show a custom introduction before a single permission, pass `overviewMode = PermissionOverviewMode.Show` and `overview = PermissionOverview { IntroPage() }`. `Show` also works with the default overview. To skip the introduction for a bundle, pass `overviewMode = PermissionOverviewMode.Hide`. Changing the mode does not change which permissions the button requests.

## Timed carousel

`HandlePermissions(autoAdvance = true)` loops through the visible pages, returning to the first after the last page's timer finishes. The segmented progress bar above the header has one equal-width segment per visible page (including the overview, when shown). Earlier segments are full, the current segment fills during its reading time, and future segments are empty; all fill at the end of the final page and reset on wraparound. The button beside the page count pauses or resumes. User interaction pauses the timer and fades the bar until the reader resumes it; the page keeps its remaining reading time. A single visible page stays still. The default is `false`. The host supplies `autoAdvanceDelayMillis` on each `PermissionDescription` and, if present, on `PermissionOverview` (six seconds by default). Use `estimateReadingDelayMillis("$title $body", ReadingPace.Slow)` to derive a delay from the same copy shown in a custom page; see the [demo Activity](../demos/). The library cannot count text inside an arbitrary composable. `ReadingPace.Slow`, `Normal`, and `Fast` use 120, 180, and 230 words per minute. For image-heavy pages or languages without word separators, set the delay directly. Autoplay pauses when the host is not resumed and is disabled for touch exploration.

See the [segmented progress preview](/android.apexfission.permissions/screenshots/segmented-reading-progress.png) for its placement above the header.

`PermissionDisplayMode.All` includes granted pages with green check badges. `MissingOnly` hides granted pages/icons. Both evaluate the **full original list** and request missing permissions together while the explanation is shown. Only required entries gate protected content. After a partial grant that leaves required access missing, the explanation remains; when all remaining permissions are inferred to lack a prompt, the action offers app settings.

`PermissionStatus.PermanentlyDenied` is only an inference from request history and the rationale signal. The default built-in note says Android **might** skip the prompt; never present the status as certain. `HandlePermissions` accepts `recoveryContent = { inferredPermissionNames -> ... }`, `settingsActionLabel`, and `onOpenSettings = { ... }`. The content appears when at least one permission is inferred blocked, even if the batch can still request another missing permission. The callback replaces the default app-details launch and should provide a user-driven Settings route. `PermissionBundleScreen` accepts the content and label hooks; its mandatory `onOpenSettings` controls the action. See the [recovery example below](#custom-recovery-example) and [runnable demo guide](../demos/).

See the [selected permission](/android.apexfission.permissions/screenshots/permission-detail.png), [settings recovery](/android.apexfission.permissions/screenshots/settings-recovery.png), and [overflowing icon strip](/android.apexfission.permissions/screenshots/overflowing-icons.png) previews for the other main UI states. All six appear together in the [screen gallery](../gallery/). They are static Compose previews, not system permission dialogs.

## Other UI entry points

- `HandlePermissions(onPermissionsResult = { results -> ... })` observes the batch result. Do not treat it as atomic.
- For camera alone, use `HandlePermissions` with one `PermissionDescription(Manifest.permission.CAMERA) { CameraExplanation() }`; the overview is hidden by Automatic unless you set `overviewMode = Show`.
- For a custom explanation page with the built-in batch button, supply `PermissionDescription(Manifest.permission.CAMERA) { CameraExplanation() }`.
- `PermissionBundleScreen` renders host-managed statuses and actions without launching Android requests.

See [the first request](../getting-started/) and [runnable demo guide](../demos/).

## Custom recovery example

```kotlin
HandlePermissions(
    permissions = listOf(PermissionDescription(Manifest.permission.CAMERA) {
        DefaultPermissionPage("Camera", Icons.Default.PhotoCamera)
    }),
    onBack = { finish() },
    onNotNow = { finish() },
    recoveryContent = { inferred ->
        Text("Android may skip the prompt. You can review camera access in Settings.")
    },
    settingsActionLabel = "Review camera access",
    onOpenSettings = { showCameraSettingsExplanation() },
) { grants -> CameraFeature() }
```

`showCameraSettingsExplanation` is host UI: offer a decline option and open app details only when the user chooses it. See [screen states](../gallery/) for the built-in recovery UI.
