---
title: Compose UI
description: Customize the overview, per-permission pages, recovery, and timing.
---

## Your page in the carousel

`PermissionDescription { ... }` supplies the entire page, including its hero and copy. The library keeps the icon strip and action button in place outside the carousel. If no page is supplied, generic localized visuals and text are used. The `label` and `icon` remain useful to the persistent strip and accessibility.

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

## Overview and visibility

When several permissions are visible, the feature overview appears automatically. Set `overviewMode = PermissionOverviewMode.Show` to include it even for a single permission, or `Hide` to start on the first permission. Customize it with `overview = PermissionOverview(page = { MyIntroduction() })`.

`displayMode = PermissionDisplayMode.All` keeps granted permission pages and adds green checks to their icons. `MissingOnly` hides their pages and icons. Filtering affects the explanation UI, not which grants are required for protected content.

## Reading progress

Set `autoAdvance = true` on `HandlePermissions` to loop through pages until the reader interacts. Each page has a six-second default, which the host can override with `autoAdvanceDelayMillis` or calculate with `estimateReadingDelayMillis`. The segmented indicator shows progress for the visible pages, fades when paused, and resets when the carousel wraps. The reader can pause and resume; a single page does not auto-scroll.

## Denial and recovery

`PermissionStatus.PermanentlyDenied` is inferred from request history and Android's rationale signal. It is **not** a definitive platform flag. The default UI cautiously says the prompt *might* be skipped. Customize the explanation with `recoveryContent = { inferredNames -> ... }`, the button with `settingsActionLabel`, and its behavior with `onOpenSettings`. Do not automatically reopen a request after a denial.

For custom controller-driven UI, `permissionContent = { controllers -> ... }` provides one `PermissionController` per entry and uses individual request actions. See the [full Compose integration guide on GitHub](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/agents/compose.md) for lifecycle details and examples.
