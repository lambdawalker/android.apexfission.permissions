# Migration and obsolete examples

Read [IMPORT.md](../../IMPORT.md) for the current published version. Historical release-to-commit mapping for the changes below has not been established; do not infer a precise first Maven version from commit order. The commits below are evidence of source changes, not release claims.

| Old pattern | Current replacement | Source change |
| --- | --- | --- |
| `CameraPermissionState` and camera-only compatibility API | `HandlePermissions` with one camera description | `254432f` removed camera compatibility |
| `HandlePermissionsIndividually` | Unified `HandlePermissions` batch flow | `1758b37` |
| Description-generated fallback page | Required `PermissionDescription.page` lambda | `6330918` |
| `HandlePermissionBundle` / separate `permissionContent` host | Put UI in each description's page | `d58bcf7` |
| Constructing an empty `PermissionOverview()` | Supply its page; omit gate's overview argument for generic default | `d58bcf7`, `e5bb5d4` |
| `DefaultPermissionPage(icon = ...)` | `DefaultPermissionPage(heroImage = ...)` | `ae7ef5a` adds image overloads |
| Root-package imports | Import from `requester`, `ui`, `recipe` | `01be774` |
| Consumer manual in root guide / `agents/` | [docs/agents/index.md](index.md); root AGENTS is maintainer-only | Current documentation consolidation |

Do not generate new code using removed camera-only or individual-flow APIs, `permissionContent`, or page-less descriptions. Current enum values are `PermissionDisplayMode.All` and `MissingOnly`; current reading helper is `estimateReadingDelayMillis`, not `computeReadingText`. Do not substitute guessed names from older examples.

```kotlin
PermissionDescription(Manifest.permission.CAMERA) {
    DefaultPermissionPage(label = "Camera", heroImage = Icons.Default.PhotoCamera)
}
```

The fragment requires Android Manifest, Compose material icon imports and `ui` types; see the [complete quickstart](quickstart.md) for an import-complete integration. Omitting overview gives a generic page only when visibility rules permit it; it does not remove the requirement for a page when explicitly constructing `PermissionOverview`.

Confirm artifact coordinates against [publication configuration](../../permission/build.gradle.kts). Namespace `com.apexfission.android.permission` is not the Maven group. Old coordinates and older documentation are not aliases guaranteed by this repository.
