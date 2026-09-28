# Agent guide

This repository provides Android runtime permission UI and callback helpers. Public APIs live in `:permission` under `com.apexfission.android.permission`. The `:app` module is a runnable example.

For a consuming app, add `implementation("com.apexfission.androi:permission:0.1.0")` and `mavenCentral()` (or `implementation(project(":permission"))` in a source checkout). The host needs minSdk 24. Declare requested permissions in the **host app's** manifest, then choose one entry point below. Start requests from a user action; Android may grant only part of a batch.

| Host need | Guide |
| --- | --- |
| Compose explanation screen that gates a feature | [Compose integration](agents/compose.md) — `HandlePermissions` |
| No library UI; ask from an Activity with callbacks | [Code-only integration](agents/code-only.md) — `PermissionRequester` |
| Check grants and own the Android launcher | [Code-only integration](agents/code-only.md) — `runIfPermissionsGranted` |
| Location, notifications, and photo access | [Platform recipes](agents/platform-recipes.md) — staged steps and photo picker |
| Choose overview visibility or inspect rendered states | [Compose integration](agents/compose.md#overview-page) and [screenshot gallery](README.md#screenshots) |
| Change this repository, test, or release | [Maintainer notes](agents/maintenance.md) |

`HandlePermissions` is the default when an explanation screen should gate a feature. Its built-in UI requests missing required and optional permissions together; only required grants gate content. For a fully custom UI, `permissionContent` supplies individual controllers and uses the individual request flow. For code-only calls, `PermissionRequester` owns an Activity result launcher, while `runIfPermissionsGranted` only checks current grants and leaves requesting to the host. The linked guides include copyable setup, lifecycle requirements, and failure paths.

Declare every requested permission in the host Android manifest. The generic request APIs target ordinary **runtime** permissions. Special app access (overlay, all-files access, exact alarms) needs a host-managed flow; [platform recipes](agents/platform-recipes.md) guide staged location, notifications, and the photo picker. Android can grant only part of a batch; protected content waits for all required grants. Each `PermissionDescription` requires a full `page` composable; mark optional access with `required = false` and read the `PermissionGrants` snapshot in `content`. See the [Compose guide](agents/compose.md#required-and-optional-permissions).

For the human-oriented overview and release setup, see [README.md](README.md). The app's [MainActivity](app/src/main/java/com/apexfission/android/permissions/MainActivity.kt) opens the [carousel demo](app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) or the [platform recipes demo](app/src/main/java/com/apexfission/android/permissions/demo/PlatformRecipesDemo.kt). The latter illustrates runtime requests, staged background access, Settings recovery, and visual media selection; copy the recipe patterns into a feature that needs them.
