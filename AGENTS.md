# Agent guide

This repository provides Android runtime permission UI and callback helpers. Public APIs live in `:permission` under `com.apexfission.android.permission`. The `:app` module is a runnable example.

| Host need | Guide |
| --- | --- |
| Compose explanation screen that gates a feature | [Compose integration](agents/compose.md) — `HandlePermissions` |
| No library UI; ask from an Activity with callbacks | [Code-only integration](agents/code-only.md) — `PermissionRequester` |
| Check grants and own the Android launcher | [Code-only integration](agents/code-only.md) — `runIfPermissionsGranted` |
| Change this repository, test, or release | [Maintainer notes](agents/maintenance.md) |

Declare every requested permission in the host Android manifest. These APIs target ordinary **runtime** permissions. Special app access (overlay, all-files access, exact alarms) and staged platform requests (such as background location) need a host-managed flow. Android can grant only part of a batch; protected content waits for all required grants.

For the human-oriented overview and release setup, see [README.md](README.md). For the runnable Compose sample, see [MainActivity.kt](app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).
