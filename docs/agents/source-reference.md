# Source declaration reference

Generated from the current development sources. See [API contracts](api.md) for supported entry points, ownership, and behavior. These excerpts are a source reference, not a binary compatibility report.

## `permission/src/main/java/com/apexfission/android/permission/recipe/PermissionRecipes.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/recipe/PermissionRecipes.kt)

```kotlin
package com.apexfission.android.permission.recipe
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class LocationAccuracy {
    Approximate,
    Precise,
}
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
```

## `permission/src/main/java/com/apexfission/android/permission/recipe/PermissionState.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/recipe/PermissionState.kt)

```kotlin
package com.apexfission.android.permission.recipe

enum class PermissionStatus {
    Granted,
    NotRequested,
    RationaleRequired,
    PermanentlyDenied,
}
```

## `permission/src/main/java/com/apexfission/android/permission/recipe/VisualMediaPicker.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/recipe/VisualMediaPicker.kt)

```kotlin
package com.apexfission.android.permission.recipe
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

enum class VisualMediaSelection {
    Image,
    Video,
    ImageOrVideo,
}
class VisualMediaPicker internal constructor(
    private val launchPicker: (VisualMediaSelection) -> Unit,
) {
    fun launch(selection: VisualMediaSelection = VisualMediaSelection.ImageOrVideo)
}
@Composable
fun rememberVisualMediaPicker(onResult: (Uri?) -> Unit): VisualMediaPicker
```

## `permission/src/main/java/com/apexfission/android/permission/requester/PermissionCheck.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/requester/PermissionCheck.kt)

```kotlin
package com.apexfission.android.permission.requester
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionCheckResult internal constructor(
    val missing: List<String>,
) {
    infix fun otherwise(onMissing: (List<String>) -> Unit): PermissionCheckResult
}
fun Context.runIfPermissionsGranted(
    vararg permissions: String,
    onGranted: () -> Unit,
): PermissionCheckResult
fun Context.runIfPermissionsGranted(
    permissions: List<String>,
    onGranted: () -> Unit,
): PermissionCheckResult
```

## `permission/src/main/java/com/apexfission/android/permission/requester/PermissionGrants.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/requester/PermissionGrants.kt)

```kotlin
package com.apexfission.android.permission.requester
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.PermissionDescription

class PermissionGrants internal constructor(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
) {
    val statusByPermission: Map<String, PermissionStatus>
    val missingRequired: Set<String>
    val missingOptional: Set<String>
    val canProceed: Boolean
        get()
    fun isGranted(permission: String): Boolean
}
```

## `permission/src/main/java/com/apexfission/android/permission/requester/PermissionHandler.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/requester/PermissionHandler.kt)

```kotlin
package com.apexfission.android.permission.requester
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.recipe.resolvePermissionStatus
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionBundleScreen
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionDisplayMode
import com.apexfission.android.permission.ui.PermissionOverview
import com.apexfission.android.permission.ui.PermissionOverviewMode
import com.apexfission.android.permission.ui.visiblePermissionIndices
import com.apexfission.android.permission.ui.defaultPermissionOverview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissions(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = defaultPermissionOverview(),
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
    displayMode: PermissionDisplayMode = PermissionDisplayMode.All,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    /** Replaces the built-in recovery note with feature-specific copy or UI. */
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    /** Label for the Settings action when a request may no longer show a system prompt. */
    settingsActionLabel: String? = null,
    /** Overrides opening the app details page. Called after the user taps the Settings action. */
    onOpenSettings: (() -> Unit)? = null,
    content: @Composable (PermissionGrants) -> Unit,
)
```

## `permission/src/main/java/com/apexfission/android/permission/requester/PermissionRecovery.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/requester/PermissionRecovery.kt)

```kotlin
package com.apexfission.android.permission.requester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.apexfission.android.permission.R

@Composable
fun DefaultPermissionRecovery(permissions: List<String>)
```

## `permission/src/main/java/com/apexfission/android/permission/requester/PermissionRequester.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/requester/PermissionRequester.kt)

```kotlin
package com.apexfission.android.permission.requester
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionRequester(activity: ComponentActivity) {
    fun requestPermissions(
        vararg permissions: String,
        onGranted: () -> Unit,
    ): PendingPermissionRequest
}
class PendingPermissionRequest internal constructor(
    private val start: ((List<String>) -> Unit) -> Unit,
) {
    infix fun onDenied(handler: (List<String>) -> Unit)
}
```

## `permission/src/main/java/com/apexfission/android/permission/ui/PermissionAutoAdvance.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/ui/PermissionAutoAdvance.kt)

```kotlin
package com.apexfission.android.permission.ui
import kotlin.math.roundToInt

enum class ReadingPace(val wordsPerMinute: Int) {
    Slow(120),
    Normal(180),
    Fast(230),
}
fun estimateReadingDelayMillis(text: String, pace: ReadingPace = ReadingPace.Slow): Long
```

## `permission/src/main/java/com/apexfission/android/permission/ui/PermissionBundleScreen.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/ui/PermissionBundleScreen.kt)

```kotlin
package com.apexfission.android.permission.ui
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.apexfission.android.permission.requester.DefaultPermissionRecovery
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.R
import com.apexfission.android.permission.recipe.allPermissionsGranted
import com.apexfission.android.permission.recipe.inferredRecoveryPermissions
import kotlinx.coroutines.launch

class PermissionOverview(
    /** Time spent on this page when autoplay is enabled. */
    val autoAdvanceDelayMillis: Long = 6_000L,
    /** Complete feature overview, including its hero and explanation. */
    val page: @Composable () -> Unit,
) {
}
data class PermissionFeature(val icon: ImageVector, val title: String, val subtitle: String)
@Composable
fun DefaultDescription(
    label: String,
    icon: ImageVector? = null,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable
fun DefaultPermissionPage(
    label: String,
    heroImage: ImageVector = Icons.Default.Lock,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable
fun DefaultPermissionPage(
    label: String,
    heroImage: Bitmap,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable
fun DefaultPermissionPage(
    label: String,
    @DrawableRes heroImage: Int,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable
fun DefaultPermissionPage(
    label: String,
    heroImage: Drawable,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
enum class PermissionDisplayMode {
    All,
    MissingOnly,
}
enum class PermissionOverviewMode {
    Automatic,
    Show,
    Hide,
}
@Composable
fun PermissionBundleScreen(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = defaultPermissionOverview(),
    onComplete: () -> Unit = {},
    initialPage: Int = 0,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    onUserInteraction: () -> Unit = {},
    initialAutoAdvanceStopped: Boolean = false,
    onAutoAdvanceResume: () -> Unit = {},
    /** Replaces the default uncertain-recovery note; receives permission strings inferred blocked. */
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    /** Overrides the Settings button caption when Settings is the primary action. */
    settingsActionLabel: String? = null,
)
```

## `permission/src/main/java/com/apexfission/android/permission/ui/PermissionDescription.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/ui/PermissionDescription.kt)

```kotlin
package com.apexfission.android.permission.ui
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

class PermissionDescription(
    val permission: String,
    label: String = DefaultPermissionLabel,
    val icon: ImageVector = PermissionVisualDefaults.forPermission(permission).icon,
    val autoAdvanceDelayMillis: Long = 6_000L,
    val required: Boolean = true,
    val page: @Composable () -> Unit,
) {
    val label: String
}
```

## `permission/src/main/java/com/apexfission/android/permission/ui/PermissionVisualDefaults.kt`

[Source](../../permission/src/main/java/com/apexfission/android/permission/ui/PermissionVisualDefaults.kt)

```kotlin
package com.apexfission.android.permission.ui
import android.Manifest
import androidx.annotation.StringRes
import com.apexfission.android.permission.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

data class PermissionVisual(val label: String, val icon: ImageVector)
object PermissionVisualDefaults {
    fun forPermission(permission: String): PermissionVisual
}
```
