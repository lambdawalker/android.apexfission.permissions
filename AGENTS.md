# Agent guide

This repository provides Android runtime permission UI and callback helpers. Public APIs live in `:permission` under `com.apexfission.android.permission`. The `:app` module is a runnable example.

| Host need | Guide |
| --- | --- |
| Compose explanation screen that gates a feature | [Compose integration](agents/compose.md) — `HandlePermissions` |
| No library UI; ask from an Activity with callbacks | [Code-only integration](agents/code-only.md) — `PermissionRequester` |
| Check grants and own the Android launcher | [Code-only integration](agents/code-only.md) — `runIfPermissionsGranted` |
| Location, notifications, and photo access | [Platform recipes](agents/platform-recipes.md) — staged steps and photo picker |
| Choose overview visibility or inspect rendered states | [Compose integration](agents/compose.md#overview-page) and [screenshot gallery](README.md#screenshots) |
| Change this repository, test, or release | [Maintainer notes](agents/maintenance.md) |

Declare every requested permission in the host Android manifest. The generic request APIs target ordinary **runtime** permissions. Special app access (overlay, all-files access, exact alarms) needs a host-managed flow; [platform recipes](agents/platform-recipes.md) guide staged location, notifications, and the photo picker. Android can grant only part of a batch; protected content waits for all required grants. Mark an optional `PermissionDescription(required = false)` and read the `PermissionGrants` snapshot in `content`; see the [Compose guide](agents/compose.md#required-and-optional-permissions).

For the human-oriented overview and release setup, see [README.md](README.md). For the runnable Compose sample, see [MainActivity.kt](app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).
