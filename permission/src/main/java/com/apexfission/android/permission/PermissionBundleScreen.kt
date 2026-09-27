package com.apexfission.android.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch

/** Content for the optional first carousel page. */
class PermissionOverview(
    val title: String? = null,
    val body: String? = null,
    val hero: (@Composable () -> Unit)? = null,
    val description: (@Composable () -> Unit)? = null,
    /** Time spent on this page when autoplay is enabled. */
    val autoAdvanceDelayMillis: Long = 6_000L,
    /** Full page composable. Replaces [hero], [description], and the default copy. */
    val page: (@Composable () -> Unit)? = null,
) {
    init { require(autoAdvanceDelayMillis > 0) { "autoAdvanceDelayMillis must be positive" } }
}

/** Reusable text body for a custom [PermissionDescription.description] or overview page. */
@Composable
fun DefaultDescription(
    label: String,
    icon: ImageVector = Icons.Default.Lock,
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
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(feature.title, fontWeight = FontWeight.SemiBold)
                    Text(feature.subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Default full page, useful inside a single [PermissionDescription.page] lambda. */
@Composable
fun DefaultPermissionPage(
    label: String,
    icon: ImageVector = Icons.Default.Lock,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
) {
    Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
        DefaultBundleHero(icon)
    }
    Spacer(Modifier.height(24.dp))
    DefaultDescription(label, icon, title, body, features)
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
    /** Show the overview when more than one permission is visible. */
    Automatic,

    /** Show the overview even for a single visible permission. */
    Show,

    /** Go directly to the first permission, even when several are visible. */
    Hide,
}

internal fun shouldShowOverview(visiblePermissionCount: Int, mode: PermissionOverviewMode): Boolean =
    when (mode) {
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
internal fun iconScrollOffsetDp(count: Int, selectedIndex: Int): Int =
    if (selectedIndex < 0) (count - 1) * 32 else selectedIndex * 64

/**
 * One action requests all runtime permissions. A multi-permission carousel starts with a feature
 * overview, then shows each permission. The carousel owns each full page. A fixed, horizontally
 * scrolling icon selector sits above the action button and centers the selected icon. On the
 * overview, the whole icon set is centered with overflow on both sides. Android can show
 * multiple dialogs and return partial grants.
 *
 * [onRequest] should launch one multiple-permission request. [onOpenSettings] should open app
 * settings if no outstanding permission can prompt. [overviewMode] can force the first page on
 * or off; the default follows the visible permission count. [initialPage] is zero for the
 * overview when it is present, otherwise zero for the first permission.
 * [autoAdvance] advances through pages using their explicit delays until the user interacts.
 * A progress bar shows the remaining reading time; the adjacent control pauses or resumes it.
 * After the last page it returns to the first. A touch, swipe, or icon selection pauses it
 * until resumed.
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
    overview: PermissionOverview = PermissionOverview(),
    onComplete: () -> Unit = {},
    initialPage: Int = 0,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    onUserInteraction: () -> Unit = {},
    initialAutoAdvanceStopped: Boolean = false,
    onAutoAdvanceResume: () -> Unit = {},
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
    val touchExplorationEnabled = (context.getSystemService(Context.ACCESSIBILITY_SERVICE)
        as? AccessibilityManager)?.isTouchExplorationEnabled == true
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
    LaunchedEffect(autoAdvance, stopped.value, resumed.value, touchExplorationEnabled,
        settled, pageCount, pageDelayMillis, progress) {
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
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (autoAdvance && pageCount > 1) {
                val progressDescription = stringResource(R.string.permission_auto_advance_progress)
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.fillMaxWidth()
                        .semantics { contentDescription = progressDescription },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { stopAutoAdvance(); onBack() }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.permission_back))
                }
                Text(stringResource(R.string.permission_progress, current + 1, pageCount),
                    style = MaterialTheme.typography.labelMedium)
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
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)
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
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when {
                        item?.page != null -> item.page.invoke()
                        item == null && overview.page != null -> overview.page.invoke()
                        else -> {
                            Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
                                when {
                                    item?.hero != null -> item.hero.invoke()
                                    item == null && overview.hero != null -> overview.hero.invoke()
                                    else -> DefaultBundleHero(item?.icon ?: Icons.Default.Lock)
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            when {
                                item?.description != null -> item.description.invoke()
                                item != null -> DefaultDescription(item.label, item.icon, item.title, item.body, item.features)
                                overview.description != null -> overview.description.invoke()
                                else -> DefaultDescription(
                            label = stringResource(R.string.permission_bundle_label),
                            title = overview.title ?: stringResource(R.string.permission_bundle_overview_title),
                            body = overview.body ?: stringResource(R.string.permission_bundle_overview_body),
                        )
                            }
                        }
                    }
                    if (item != null && statuses[index] == PermissionStatus.Granted) {
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.permission_bundle_granted),
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    when (action) {
                        BundleAction.Request -> if (statuses.any {
                            it == PermissionStatus.RationaleRequired || it == PermissionStatus.PermanentlyDenied
                        }) stringResource(R.string.permission_bundle_retry)
                        else stringResource(R.string.permission_bundle_request)
                        BundleAction.Settings -> stringResource(R.string.permission_open_settings)
                        BundleAction.Complete -> stringResource(R.string.permission_done)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(onClick = { stopAutoAdvance(); onNotNow() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(stringResource(R.string.camera_permission_not_now))
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
            modifier = Modifier.fillMaxWidth().horizontalScroll(scroll),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(edgeSpace))
            permissions.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val size by animateDpAsState(if (selected) 56.dp else 44.dp, label = "bundle icon size")
            Box(
                modifier = Modifier.padding(horizontal = 4.dp).size(56.dp)
                    .semantics { contentDescription = item.label }
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(size).background(
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
                        modifier = Modifier.align(Alignment.BottomEnd).size(20.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null,
                            modifier = Modifier.size(16.dp), tint = Color(0xFF16803A))
                    }
                }
            }
            }
            Spacer(Modifier.width(edgeSpace))
        }
    }
}

@Composable
private fun DefaultBundleHero(icon: ImageVector) {
    Box(
        modifier = Modifier.size(width = 260.dp, height = 200.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/**
 * Requests a nonempty, unique list of manifest-declared Android runtime permissions in one batch
 * after a button tap. [overview] customizes the optional opening page. [overviewMode] controls
 * whether it appears; Automatic preserves the traditional multiple-permission default. Each
 * [PermissionDescription] accepts one full `page` composable; the strip remains fixed below it.
 * [content] is shown only while all permissions are granted. A callback result can be partial;
 * permission status is checked again on recomposition and return from Settings.
 * [displayMode] filters only presentation: the underlying batch request and grant gate still
 * use every [permissions] entry. After the visible set changes, the carousel starts at its
 * overview (or sole remaining permission).
 * [autoAdvance] is opt-in and uses each page's `autoAdvanceDelayMillis`. User interaction
 * permanently stops it for this screen visit, including after a partial permission result.
 *
 * Special app access and permissions with platform-specific sequencing need host-managed flows.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissionBundle(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = PermissionOverview(),
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
    displayMode: PermissionDisplayMode = PermissionDisplayMode.All,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    content: @Composable () -> Unit,
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    val names = permissions.map { it.permission }
    require(names.distinct().size == names.size && names.all { it.isNotBlank() }) {
        "Permissions must have unique nonblank names"
    }
    val context = LocalContext.current.applicationContext
    val history = remember(context) {
        context.getSharedPreferences("camera_permission_state", Context.MODE_PRIVATE)
    }
    val requested = remember(names) {
        mutableStateMapOf<String, Boolean>().apply {
            names.forEach { put(it, history.getBoolean(it, false)) }
        }
    }
    val autoAdvanceStopped = rememberSaveable { mutableStateOf(false) }
    val currentResultCallback by rememberUpdatedState(onPermissionsResult)
    val state = rememberMultiplePermissionsState(permissions = names, onPermissionsResult = { results ->
        results.forEach { (name, granted) ->
            if (granted) {
                requested[name] = false
                history.edit().putBoolean(name, false).apply()
            }
        }
        currentResultCallback(results)
    })
    val permissionStates = state.permissions.associateBy { it.permission }
    val statuses = names.map { name ->
        val permission = requireNotNull(permissionStates[name]) { "Missing permission state for $name" }
        resolvePermissionStatus(
            granted = permission.status.isGranted,
            shouldShowRationale = permission.status.shouldShowRationale,
            requestedBefore = requested[name] == true,
        )
    }
    LaunchedEffect(statuses, names) {
        names.zip(statuses).forEach { (name, status) ->
            if (status == PermissionStatus.Granted && requested[name] == true) {
                requested[name] = false
                history.edit().putBoolean(name, false).apply()
            }
        }
    }
    if (allPermissionsGranted(statuses)) {
        content()
    } else {
        val visibleIndices = visiblePermissionIndices(statuses, displayMode)
        val visibleNames = visibleIndices.map(names::get)
        key(visibleNames, overviewMode) {
            PermissionBundleScreen(
                permissions = visibleIndices.map(permissions::get),
                statuses = visibleIndices.map(statuses::get),
                onBack = onBack,
                onNotNow = onNotNow,
                onRequest = {
                    names.zip(statuses).filter { it.second != PermissionStatus.Granted }.forEach { (name, _) ->
                        requested[name] = true
                        history.edit().putBoolean(name, true).apply()
                    }
                    state.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                overview = overview,
                overviewMode = overviewMode,
                autoAdvance = autoAdvance,
                onUserInteraction = { autoAdvanceStopped.value = true },
                initialAutoAdvanceStopped = autoAdvanceStopped.value,
                onAutoAdvanceResume = { autoAdvanceStopped.value = false },
                modifier = modifier,
            )
        }
    }
}
