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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.font.FontWeight
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

/** Complete first carousel page. Callers constructing an overview must supply its content. */
class PermissionOverview(
    /** Time spent on this page when autoplay is enabled. */
    val autoAdvanceDelayMillis: Long = 6_000L,
    /** Complete feature overview, including its hero and explanation. */
    val page: @Composable () -> Unit,
) {
    init {
        require(autoAdvanceDelayMillis > 0) { "autoAdvanceDelayMillis must be positive" }
    }
}

/** Generic opening page used when the host does not supply an overview. */
internal fun defaultPermissionOverview(): PermissionOverview = PermissionOverview {
    DefaultPermissionPage(
        label = stringResource(R.string.permission_bundle_label),
        heroImage = Icons.Default.Lock,
        title = stringResource(R.string.permission_bundle_overview_title),
        body = stringResource(R.string.permission_bundle_overview_body),
    )
}

/** A supporting benefit shown below a permission's explanation. */
data class PermissionFeature(val icon: ImageVector, val title: String, val subtitle: String)

/** Reusable text body for a [PermissionDescription.page] or overview page. */
@Composable
fun DefaultDescription(
    label: String,
    icon: ImageVector? = null,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
) {
    // The icon is accepted for convenient reuse of metadata; the selector renders it above.
    Text(
        title ?: stringResource(R.string.permission_generic_title, label),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        body ?: stringResource(R.string.permission_generic_body, label),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
    if (features.isNotEmpty()) {
        Spacer(Modifier.height(24.dp))
        features.forEach { feature ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon ?: feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(feature.title, fontWeight = FontWeight.SemiBold)
                    Text(feature.subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Default full page with a vector hero. [heroImage] is decorative; the text explains access. */
@Composable
fun DefaultPermissionPage(
    label: String,
    heroImage: ImageVector = Icons.Default.Lock,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
) {
    DefaultPermissionPageLayout(label, title, body, features) {
        DefaultHeroFrame {
            Icon(heroImage, contentDescription = null, modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

/** Android [Bitmap] hero; the caller retains ownership and must keep it valid while composed. */
@Composable
fun DefaultPermissionPage(
    label: String,
    heroImage: Bitmap,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
) {
    DefaultPermissionPageLayout(label, title, body, features) {
        HeroPainter(BitmapPainter(heroImage.asImageBitmap()))
    }
}

/** Drawable resource hero (vector or bitmap); pass `R.drawable.your_artwork`. */
@Composable
fun DefaultPermissionPage(
    label: String,
    @DrawableRes heroImage: Int,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
) {
    val painter = painterResource(heroImage)
    DefaultPermissionPageLayout(label, title, body, features) { HeroPainter(painter) }
}

/** Android [Drawable] instance hero. The host owns its lifecycle. */
@Composable
fun DefaultPermissionPage(
    label: String,
    heroImage: Drawable,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
) {
    DefaultPermissionPageLayout(label, title, body, features) {
        DefaultHeroFrame {
            AndroidView(
                factory = { context -> ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                } },
                update = { it.setImageDrawable(heroImage) },
                modifier = Modifier.size(width = 212.dp, height = 152.dp),
            )
        }
    }
}

@Composable
private fun DefaultPermissionPageLayout(
    label: String,
    title: String?,
    body: String?,
    features: List<PermissionFeature>,
    hero: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) { hero() }
    Spacer(Modifier.height(24.dp))
    DefaultDescription(label = label, title = title, body = body, features = features)
}

@Composable
private fun HeroPainter(painter: Painter) {
    DefaultHeroFrame {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 212.dp, height = 152.dp),
        )
    }
}

internal enum class BundleAction { Request, Settings, Complete }

/** Which permissions appear in the built-in explanation carousel and icon strip. */
enum class PermissionDisplayMode {
    /** Include granted permissions and mark them with a green check badge. */
    All,

    /** Omit granted permissions; only missing permissions have carousel pages and icons. */
    MissingOnly,
}

/** Controls whether the feature overview is a carousel page. */
enum class PermissionOverviewMode {
    /** Show the overview when more than one permission is visible; a generic one is supplied by default. */
    Automatic,

    /** Show the overview even for a single visible permission. */
    Show,

    /** Go directly to the first permission, even when several are visible. */
    Hide,
}

internal fun shouldShowOverview(
    visiblePermissionCount: Int,
    mode: PermissionOverviewMode,
): Boolean = when (mode) {
    PermissionOverviewMode.Automatic -> visiblePermissionCount > 1
    PermissionOverviewMode.Show -> true
    PermissionOverviewMode.Hide -> false
}

internal fun visiblePermissionIndices(
    statuses: List<PermissionStatus>,
    displayMode: PermissionDisplayMode,
): List<Int> = statuses.indices.filter { index ->
    displayMode == PermissionDisplayMode.All || statuses[index] != PermissionStatus.Granted
}

internal fun bundleAction(statuses: List<PermissionStatus>): BundleAction = when {
    allPermissionsGranted(statuses) -> BundleAction.Complete
    statuses.any { it == PermissionStatus.NotRequested || it == PermissionStatus.RationaleRequired } -> BundleAction.Request
    else -> BundleAction.Settings
}

/** 64 dp icon slots: center one slot, or center the full row on the overview. */
internal fun iconScrollOffsetDp(count: Int, selectedIndex: Int): Int = if (selectedIndex < 0) (count - 1) * 32 else selectedIndex * 64

/**
 * One action requests all runtime permissions. A generic or host-supplied overview may precede the
 * permission pages. The carousel owns each full page. A fixed, horizontally
 * scrolling icon selector sits above the action button and centers the selected icon. On the
 * overview, the whole icon set is centered with overflow on both sides. Android can show
 * multiple dialogs and return partial grants.
 *
 * [onRequest] should launch one multiple-permission request. [onOpenSettings] should open app
 * settings if no outstanding permission can prompt. [overviewMode] controls the overview;
 * Automatic shows it when more than one permission is visible. Hide omits it.
 * [initialPage] is zero for the
 * overview when it is present, otherwise zero for the first permission.
 * [autoAdvance] advances through pages using their explicit delays until the user interacts.
 * A segmented progress bar shows the remaining reading time; the adjacent control pauses or resumes it.
 * After the last page it returns to the first. A touch, swipe, or icon selection pauses it
 * until resumed.
 * [recoveryContent] is shown for statuses inferred to need recovery, including mixed batches.
 * [settingsActionLabel] changes the Settings button caption; [onOpenSettings] owns its action.
 */
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
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.size == statuses.size) { "Each permission needs one status" }
    require(permissions.map { it.permission }.distinct().size == permissions.size) { "Permissions must be unique" }
    val hasOverview = shouldShowOverview(permissions.size, overviewMode)
    val pageCount = permissions.size + if (hasOverview) 1 else 0
    require(initialPage in 0 until pageCount) { "initialPage is out of range" }
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val action = bundleAction(statuses)
    val recoveryPermissions = inferredRecoveryPermissions(permissions.map { it.permission }, statuses)
    val current = pager.currentPage.coerceIn(0 until pageCount)
    val stopped = rememberSaveable { mutableStateOf(initialAutoAdvanceStopped) }
    val currentOnUserInteraction = rememberUpdatedState(onUserInteraction)
    val currentOnAutoAdvanceResume = rememberUpdatedState(onAutoAdvanceResume)
    val stopAutoAdvance = {
        if (!stopped.value) {
            stopped.value = true
            currentOnUserInteraction.value()
        }
    }
    val context = LocalContext.current
    val touchExplorationEnabled =
        (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager)?.isTouchExplorationEnabled == true
    val lifecycleOwner = LocalLifecycleOwner.current
    val resumed = remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            resumed.value = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val settled = pager.settledPage
    val pageDelayMillis = if (hasOverview && settled == 0) overview.autoAdvanceDelayMillis
    else permissions[settled.coerceIn(0, pageCount - 1) - if (hasOverview) 1 else 0].autoAdvanceDelayMillis
    val nextPage = nextAutoAdvancePage(settled, pageCount)
    val progress = remember(settled, pageDelayMillis, pageCount) {
        Animatable(if (nextPage == null) 1f else 0f)
    }
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (stopped.value || touchExplorationEnabled) 0f else 1f,
        label = "Reading progress visibility",
    )
    LaunchedEffect(
        autoAdvance, stopped.value, resumed.value, touchExplorationEnabled, settled, pageCount, pageDelayMillis, progress
    ) {
        if (!autoAdvance || stopped.value || !resumed.value || touchExplorationEnabled) return@LaunchedEffect
        val next = nextPage ?: return@LaunchedEffect
        val remainingMillis = remainingAutoAdvanceMillis(progress.value, pageDelayMillis)
        progress.animateTo(1f, tween(durationMillis = remainingMillis))
        if (!stopped.value && resumed.value && !pager.isScrollInProgress) {
            if (next == 0) pager.scrollToPage(0) else pager.animateScrollToPage(next)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (autoAdvance && pageCount > 1) {
                SegmentedReadingProgressIndicator(
                    pageCount = pageCount,
                    currentPage = settled,
                    progress = progress.value,
                    indicatorAlpha = indicatorAlpha,
                    paused = stopped.value || touchExplorationEnabled,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { stopAutoAdvance(); onBack() }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.permission_back))
                }
                Text(
                    stringResource(R.string.permission_progress, current + 1, pageCount), style = MaterialTheme.typography.labelMedium
                )
                if (autoAdvance && nextPage != null && !touchExplorationEnabled) {
                    val paused = stopped.value
                    IconButton(onClick = {
                        if (stopped.value) {
                            stopped.value = false
                            currentOnAutoAdvanceResume.value()
                        } else {
                            stopAutoAdvance()
                        }
                    }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = stringResource(
                                if (paused) R.string.permission_auto_advance_resume
                                else R.string.permission_auto_advance_pause
                            ),
                        )
                    }
                } else {
                    Spacer(Modifier.size(44.dp))
                }
            }
            HorizontalPager(
                state = pager,
                key = { if (hasOverview && it == 0) "overview" else permissions[it - if (hasOverview) 1 else 0].permission },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .onPreviewKeyEvent { stopAutoAdvance(); false }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.any { it.pressed && !it.previousPressed }) stopAutoAdvance()
                            }
                        }
                    },
            ) { page ->
                val index = page - if (hasOverview) 1 else 0
                val item = permissions.getOrNull(index)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when {
                        item != null -> item.page()
                        else -> overview.page()
                    }
                    if (item != null && statuses[index] == PermissionStatus.Granted) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.permission_bundle_granted),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (item != null && !item.required) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.permission_optional),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (recoveryPermissions.isNotEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    recoveryContent(recoveryPermissions)
                }
            }
            PermissionIconStrip(permissions, statuses, if (hasOverview) current - 1 else current) { selected ->
                stopAutoAdvance()
                scope.launch { pager.animateScrollToPage(selected + if (hasOverview) 1 else 0) }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    stopAutoAdvance()
                    when (action) {
                        BundleAction.Request -> onRequest()
                        BundleAction.Settings -> onOpenSettings()
                        BundleAction.Complete -> onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    when (action) {
                        BundleAction.Request -> if (statuses.any {
                                it == PermissionStatus.RationaleRequired || it == PermissionStatus.PermanentlyDenied
                            }) stringResource(R.string.permission_bundle_retry)
                        else stringResource(R.string.permission_bundle_request)

                        BundleAction.Settings -> settingsActionLabel ?: stringResource(R.string.permission_open_settings)
                        BundleAction.Complete -> stringResource(R.string.permission_done)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(
                onClick = { stopAutoAdvance(); onNotNow() }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(stringResource(R.string.permission_not_now))
            }
        }
    }
}

/** One reading-time segment per carousel page; completed pages remain filled until wraparound. */
@Composable
private fun SegmentedReadingProgressIndicator(
    pageCount: Int,
    currentPage: Int,
    progress: Float,
    indicatorAlpha: Float,
    paused: Boolean,
) {
    val progressDescription = stringResource(R.string.permission_auto_advance_progress)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = indicatorAlpha }
            .clearAndSetSemantics {
                if (!paused) {
                    contentDescription = progressDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                }
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(pageCount) { page ->
            Canvas(Modifier.weight(1f).height(4.dp)) {
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(trackColor, cornerRadius = radius)
                val filledWidth = size.width * segmentProgressForPage(page, currentPage, progress)
                if (filledWidth > 0f) {
                    drawRoundRect(fillColor, size = Size(filledWidth, size.height), cornerRadius = radius)
                }
            }
        }
    }
}

@Composable
private fun PermissionIconStrip(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val edgeSpace = ((maxWidth - 64.dp) / 2).coerceAtLeast(0.dp)
        val offset = with(LocalDensity.current) {
            iconScrollOffsetDp(permissions.size, selectedIndex).dp.roundToPx()
        }
        val scroll = rememberScrollState(initial = offset)
        LaunchedEffect(offset) { scroll.animateScrollTo(offset) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(edgeSpace))
            permissions.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val size by animateDpAsState(if (selected) 56.dp else 44.dp, label = "bundle icon size")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(56.dp)
                        .semantics { contentDescription = item.label }
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (selected) 27.dp else 22.dp),
                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (statuses[index] == PermissionStatus.Granted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF16803A)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(edgeSpace))
        }
    }
}

@Composable
private fun DefaultHeroFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 260.dp, height = 200.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
