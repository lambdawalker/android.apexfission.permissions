---
title: Compose UI
description: Customize the overview, per-permission pages, recovery, and timing.
---

## Your page in the carousel

`PermissionDescription { ... }` requires the entire page, including its hero and copy. The library keeps the icon strip and action button in place outside the carousel. `DefaultPermissionPage` is a reusable helper you can call within the lambda. `label` and `icon` default to permission-specific metadata for the persistent strip and accessibility; override them as needed.

The page's `heroImage` can be an `ImageVector`, Android `Bitmap`, `Drawable`, or drawable resource ID such as `R.drawable.scan_hero`. The `PermissionDescription.icon` remains the separate, smaller icon in the strip. Keep a supplied bitmap alive while the page uses it.

```kotlin
PermissionDescription(
    permission = Manifest.permission.CAMERA,
    label = "Camera",
    icon = Icons.Default.PhotoCamera,
    autoAdvanceDelayMillis = estimateReadingDelayMillis(
        "$title $body", ReadingPace.Slow
    ),
) {
    DefaultPermissionPage("Camera", Icons.Default.PhotoCamera, title, body)
}
```

Here `title` and `body` are values from the host. `estimateReadingDelayMillis` lives in the `.ui` package and uses the supplied text; the library cannot inspect text inside an arbitrary composable.

### Replace `DefaultPermissionPage` entirely

The [runnable carousel demo](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) keeps its camera page on `DefaultPermissionPage` but renders the microphone page with its own Compose layout:

```kotlin
val title = "Record narration"
val body = "Allow microphone access to add narration when you record a scan."

PermissionDescription(
    permission = Manifest.permission.RECORD_AUDIO,
    label = "Microphone",
    icon = Icons.Default.Mic,
    required = false,
    autoAdvanceDelayMillis = estimateReadingDelayMillis("$title $body", ReadingPace.Slow),
) {
    NarrationPermissionPage(title, body)
}

@Composable
fun NarrationPermissionPage(title: String, body: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().height(210.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
```

Add the usual Compose layout, shape, Material 3, and `TextAlign` imports. The `label` and `icon` remain metadata for the persistent selector; the library still provides the batch button, progress, and recovery UI. The custom page itself never launches a permission request.

## Overview and visibility

The carousel has a generic lock-icon overview by default. `Automatic` shows it when several permissions are visible; `Show` includes it even for a single permission, and `Hide` skips it. Pass `overview = PermissionOverview { MyIntroduction() }` to replace the complete page, or set `overviewMode = PermissionOverviewMode.Hide` to omit it. A caller-created overview must provide its page.

`displayMode = PermissionDisplayMode.All` keeps granted permission pages and adds green checks to their icons. `MissingOnly` hides their pages and icons. Filtering affects the explanation UI, not which grants are required for protected content.

## Reading progress

Set `autoAdvance = true` on `HandlePermissions` to loop through pages until the reader interacts. Each page has a six-second default, which the host can override with `autoAdvanceDelayMillis` or calculate with `estimateReadingDelayMillis`. The segmented indicator shows progress for the visible pages, fades when paused, and resets when the carousel wraps. The reader can pause and resume; a single page does not auto-scroll.

## Denial and recovery

`PermissionStatus.PermanentlyDenied` is inferred from request history and Android's rationale signal. It is **not** a definitive platform flag. The default UI cautiously says the prompt *might* be skipped. Customize the explanation with `recoveryContent = { inferredNames -> ... }`, the button with `settingsActionLabel`, and its behavior with `onOpenSettings`. Do not automatically reopen a request after a denial.

For a custom explanation, provide any Compose UI in `PermissionDescription { ... }`; the library retains the batch request button. For a host-owned request flow without the library screen, use `PermissionRequester` from an Activity. See the [full Compose integration guide on GitHub](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/agents/compose.md) for lifecycle details and examples.
