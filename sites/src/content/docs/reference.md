---
title: Source and API
description: Find the public entry points, source documentation, and runnable demos.
---

The site is an integration guide. KDocs live beside the Kotlin declarations and remain the source of truth for each parameter and return type.

| Need | Entry point | Source |
| --- | --- | --- |
| Gate a Compose feature | `HandlePermissions` | [PermissionHandler.kt](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/permission/src/main/java/com/apexfission/android/permission/requester/PermissionHandler.kt) |
| Describe each screen | `PermissionDescription` | [PermissionScreen.kt](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/permission/src/main/java/com/apexfission/android/permission/ui/PermissionScreen.kt) |
| Own the Compose request controls | `rememberPermissionController` | [PermissionState.kt](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/permission/src/main/java/com/apexfission/android/permission/recipe/PermissionState.kt) |
| Request with Activity callbacks | `PermissionRequester` | [PermissionRequester.kt](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/permission/src/main/java/com/apexfission/android/permission/requester/PermissionRequester.kt) |
| Check without UI | `runIfPermissionsGranted` | [PermissionCheck.kt](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/permission/src/main/java/com/apexfission/android/permission/requester/PermissionCheck.kt) |
| Handle staged platform flows | `PermissionRecipes` | [PermissionRecipes.kt](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/permission/src/main/java/com/apexfission/android/permission/recipe/PermissionRecipes.kt) |

The [sample app](https://github.com/lambdawalker/android.apexfission.permissions/tree/main/app/src/main/java/com/apexfission/android/permissions) contains a carousel demo and platform recipe demo. For complete usage and release notes, see the [repository README](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/README.md).
