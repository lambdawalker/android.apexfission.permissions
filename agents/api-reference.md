# Public API declarations

Use this as a quick map of callable library APIs. Names are in `com.apexfission.android.permission`; import from the subpackage shown in each heading. Default arguments and lifecycle behavior are explained in [Compose integration](compose.md), [code-only integration](code-only.md), and [platform recipes](platform-recipes.md). Declarations below omit KDoc and annotations where they do not affect calling syntax. `defaultPermissionOverview()` is **internal**: callers omit `overview` to get it; they cannot import the factory.

## `requester`: Compose gate

```kotlin
@Composable
fun HandlePermissions(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = /* library default */,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
    displayMode: PermissionDisplayMode = PermissionDisplayMode.All,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    settingsActionLabel: String? = null,
    onOpenSettings: (() -> Unit)? = null,
    content: @Composable (PermissionGrants) -> Unit,
)
```

`HandlePermissions` owns the Android batch launcher and gates `content` on **required** grants. `onPermissionsResult` can contain partial grants; use the current `PermissionGrants` in `content`. `onBack` and `onNotNow` are host navigation callbacks. `overviewMode` alone controls visibility: `Automatic` when more than one permission is visible, `Show` always, `Hide` never. The default overview is a generic lock-icon page. Requests start on the button, not during composition. The host declares every permission in its manifest. Special app access and staged requests need separate host flows.

```kotlin
class PermissionGrants internal constructor(/* library-owned snapshot */) {
    val statusByPermission: Map<String, PermissionStatus>
    val missingRequired: Set<String>
    val missingOptional: Set<String>
    val canProceed: Boolean
    fun isGranted(permission: String): Boolean
}

@Composable fun DefaultPermissionRecovery(permissions: List<String>)
```

The recovery note is intentionally uncertain: `PermanentlyDenied` is inferred from local request history and Android's rationale signal. Supply `recoveryContent`, `settingsActionLabel`, and `onOpenSettings` when the feature needs its own explanation and Settings route.

## `ui`: pages and carousel

```kotlin
class PermissionDescription(
    val permission: String,
    val label: String = /* permission visual default */,
    val icon: ImageVector = /* permission visual default */,
    val autoAdvanceDelayMillis: Long = 6_000L,
    val required: Boolean = true,
    val page: @Composable () -> Unit,
)

class PermissionOverview(
    val autoAdvanceDelayMillis: Long = 6_000L,
    val page: @Composable () -> Unit,
)

enum class PermissionDisplayMode { All, MissingOnly }
enum class PermissionOverviewMode { Automatic, Show, Hide }

data class PermissionFeature(val icon: ImageVector, val title: String, val subtitle: String)

@Composable fun DefaultPermissionPage(
    label: String,
    icon: ImageVector = Icons.Default.Lock,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)

@Composable fun DefaultDescription(
    label: String,
    icon: ImageVector? = null,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)

data class PermissionVisual(val label: String, val icon: ImageVector)
object PermissionVisualDefaults {
    fun forPermission(permission: String): PermissionVisual
}
```

Every `PermissionDescription` and caller-created `PermissionOverview` requires a **whole page** composable. The library's icon strip, buttons, progress, and recovery text sit outside the page. `DefaultPermissionPage` is optional: it draws a centered icon hero and explanatory text. Compose your own `page` for another layout. `label` and `icon` supply the icon strip, with common Android permission defaults; override them for feature wording or localization. `DefaultDescription` is the text portion of the built-in page. `PermissionFeature` adds benefit rows there. `All` retains granted pages and adds green checks; `MissingOnly` hides granted pages but does not change the batch or grant checks.

```kotlin
@Composable fun PermissionBundleScreen(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = /* library default */,
    onComplete: () -> Unit = {},
    initialPage: Int = 0,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    onUserInteraction: () -> Unit = {},
    initialAutoAdvanceStopped: Boolean = false,
    onAutoAdvanceResume: () -> Unit = {},
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    settingsActionLabel: String? = null,
)

enum class ReadingPace(val wordsPerMinute: Int) {
    Slow(120), Normal(180), Fast(230)
}
fun estimateReadingDelayMillis(text: String, pace: ReadingPace = ReadingPace.Slow): Long
```

`PermissionBundleScreen` only renders statuses and invokes host actions; use it when the host owns the launcher. Match status order to permission order. The reading helper counts whitespace-delimited words, adds two seconds, and clamps to 4–60 seconds. It cannot inspect arbitrary Compose content. Pass the same **visible copy** to the helper and to the page:

```kotlin
val title = "Record narration"
val body = "Add a voice note to your scan."
PermissionDescription(
    permission = Manifest.permission.RECORD_AUDIO,
    required = false,
    autoAdvanceDelayMillis = estimateReadingDelayMillis("$title $body", ReadingPace.Slow),
) { NarrationPermissionPage(title, body) }
```

`NarrationPermissionPage` is a **demo composable in `:app`**, not a library API. Its recording-console card, waveform, and left-aligned text demonstrate when a custom page is useful. See [the complete demo source](../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) and [the page selection guide](compose.md#choose-a-page-layout). Use an explicit delay for visual-heavy pages or languages without whitespace word boundaries.

## `requester`: code-only requests and checks

```kotlin
class PermissionRequester(activity: ComponentActivity) {
    fun requestPermissions(vararg permissions: String, onGranted: () -> Unit): PendingPermissionRequest
}
class PendingPermissionRequest internal constructor(/* library-owned */) {
    infix fun onDenied(handler: (List<String>) -> Unit)
}

fun Context.runIfPermissionsGranted(
    vararg permissions: String, onGranted: () -> Unit,
): PermissionCheckResult
fun Context.runIfPermissionsGranted(
    permissions: List<String>, onGranted: () -> Unit,
): PermissionCheckResult
class PermissionCheckResult internal constructor(/* library-owned */) {
    val missing: List<String>
    infix fun otherwise(onMissing: (List<String>) -> Unit): PermissionCheckResult
}
```

Construct `PermissionRequester` as an Activity property before STARTED. `requestPermissions(...) { ... } onDenied { missing -> ... }` starts on `onDenied`; one request can be outstanding. `runIfPermissionsGranted(...) { ... } otherwise { missing -> ... }` is a synchronous check; the host owns any launcher and must recheck after a result. See [copyable calls and lifecycle rules](code-only.md).

## `recipe`: staged access and media selection

```kotlin
enum class PermissionStatus { Granted, NotRequested, RationaleRequired, PermanentlyDenied }
enum class LocationAccuracy { Approximate, Precise }
sealed interface PermissionRecipeStep {
    data object Ready : PermissionRecipeStep
    data class RequestRuntime(val permissions: List<String>) : PermissionRecipeStep
    data object OpenAppSettings : PermissionRecipeStep
    data object SystemControlledPrompt : PermissionRecipeStep
}
object PermissionRecipes {
    fun foregroundLocation(context: Context, accuracy: LocationAccuracy): PermissionRecipeStep
    fun backgroundLocation(context: Context, accuracy: LocationAccuracy): PermissionRecipeStep
    fun notifications(context: Context): PermissionRecipeStep
    fun grantedLocationAccuracy(context: Context): LocationAccuracy?
}
fun Context.openPermissionRecipeSettings()

enum class VisualMediaSelection { Image, Video, ImageOrVideo }
class VisualMediaPicker internal constructor(/* library-owned */) {
    fun launch(selection: VisualMediaSelection = VisualMediaSelection.ImageOrVideo)
}
@Composable fun rememberVisualMediaPicker(onResult: (Uri?) -> Unit): VisualMediaPicker
```

Recipes return the **next host action**; call again after a grant or Settings return. Foreground and background location are staged separately. The photo picker needs no broad media permission for user-selected items. See [version-specific guidance](platform-recipes.md).
