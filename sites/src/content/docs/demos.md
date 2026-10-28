---
title: Runnable demos
description: Explore the Compose carousel, platform recipes, and two code-only request styles.
---

The `:app` module is a sample with five feature destinations and a separate complete quickstart Activity. Run it from Android Studio or `./gradlew :app:installDebug`, then choose a card on the home screen. It declares camera, microphone, coarse and fine location, background location, and notifications in its manifest. Each Android permission prompt belongs to the device and appears only after a user action.

| Screen | What to try | Library concepts | Full code |
| --- | --- | --- | --- |
| Permission carousel | Swipe between the overview, camera, and optional microphone; pause or resume auto-advance; deny to inspect recovery. | `HandlePermissions`, required/optional grants, custom page, icon strip, timing, Settings callback. | [Carousel demo](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) |
| Single permission (no overview) | Choose **Open single demo**; request camera access with one page and no icon strip. | `HandlePermissions`, `PermissionOverviewMode.Hide`, custom Settings action. | [Single-permission demo](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/SinglePermissionDemo.kt) |
| Platform recipes | Check approximate and precise location, staged background access, notifications, and the photo picker. | `PermissionRecipes`, `PermissionRecipeStep`, `rememberVisualMediaPicker`. | [Recipes demo](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/PlatformRecipesDemo.kt) |
| Code-only requests | Compare a host-owned result launcher with `PermissionRequester` and inspect denial results. | `runIfPermissionsGranted { } otherwise { }`, `requestPermissions { } onDenied { }`. | [Callbacks demo](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/CodeOnlyDemo.kt) |
| Hero artwork gallery | Compare every `DefaultPermissionPage` hero form without requesting permissions. | `ImageVector`, Android `Bitmap`, drawable resource ID, Android `Drawable`; the strip icon remains separate. | [Artwork demo](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/HeroArtworkDemo.kt) |

## Permission carousel

The camera page calls `DefaultPermissionPage`; its `heroImage` is an `ImageVector`. The microphone page instead calls a host-owned `NarrationPermissionPage` with a recording-console card and waveform. The overview is custom, camera is required, microphone is optional. The demo uses `estimateReadingDelayMillis` for each visible page and opts in to automatic advancement. The one request action asks for missing permissions together, while Android may present more than one dialog.

If camera is already granted, the protected success screen appears even when optional microphone access is missing. That screen reads `PermissionGrants.isGranted(RECORD_AUDIO)` to adjust its copy. See [Compose integration](../compose/) and [the gallery](../gallery/) for details and static screen states.

## Single permission

Choose **Open single demo** on the home screen. `SinglePermissionDemo` requests camera access with `overviewMode = PermissionOverviewMode.Hide`. With one visible page, the icon strip is omitted; the camera hero and request button remain. Granting access shows “Camera permission granted!”; existing access shows that content immediately. Back and Not Now return home, and the recovery action opens app Settings. This demo and the one-page strip behavior were added after the source published as 0.2.4; use the development checkout to try them.

## Platform recipes

The recipe screen calls the appropriate next-step function on entry and after a result or return from Settings. The background-location confirmation has a decline action; it never batches foreground and background location. Notifications use Android's runtime prompt only when the platform and target allow the host to choose its timing. The photo picker selects one image or video without broad media access. See [platform recipes](../recipes/) for manifest and version-specific behavior.

## Code-only requests

The callbacks screen offers two actions. One checks camera access with `runIfPermissionsGranted`, then sends missing names to a host-owned `RequestMultiplePermissions` launcher. Its result callback rechecks current access before declaring success. The other uses an Activity-owned `PermissionRequester` registered before `STARTED`; its terminal infix `onDenied` starts a camera-and-microphone request. Both routes report the names still missing after denial and avoid re-prompting automatically. See [code-only integration](../callbacks/) for copyable examples.

## Hero artwork gallery

This static screen puts four `DefaultPermissionPage` cards in a scrollable column. It passes a Material `ImageVector`, a host-created `Bitmap`, an Android drawable resource ID, and a `GradientDrawable` instance through the same `heroImage` argument. The first keeps its Material tint; the others fit inside the hero frame with their original colors. The page's `heroImage` does not replace `PermissionDescription.icon`, which supplies the small icon in the carousel strip. See [Compose page options](../compose/#choose-a-page-layout) and [all overloads](../reference/#ui-pages-and-carousel).

The carousel, single-permission, recipes, and callbacks demos use actual Android permission requests; the artwork gallery is a static comparison. For screenshots of the library-owned Compose explanation states, see the [gallery](../gallery/). The [device tests](../development/#device-tests) exercise real denial and recovery behavior on emulators.

## Complete first request

Install with `./gradlew :app:installDebug`, launch the app, and choose **Open quickstart**. This opens `CameraQuickstartActivity`, whose entire source is extracted into [First request](../getting-started/). Try missing camera access, grant, denial, and existing access; the Activity displays the current grant and deliberately does not start camera hardware. [Full Activity source](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/app/src/main/java/com/apexfission/android/permissions/demo/CameraQuickstartActivity.kt).
