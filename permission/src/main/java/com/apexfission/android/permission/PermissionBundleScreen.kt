package com.apexfission.android.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch

/** Content for the first carousel page when more than one permission is requested. */
class PermissionOverview(
    val title: String? = null,
    val body: String? = null,
    val hero: (@Composable () -> Unit)? = null,
    val description: (@Composable () -> Unit)? = null,
)

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

internal enum class BundleAction { Request, Settings, Complete }

internal fun bundleAction(statuses: List<PermissionStatus>): BundleAction = when {
    allPermissionsGranted(statuses) -> BundleAction.Complete
    statuses.any { it == PermissionStatus.NotRequested || it == PermissionStatus.RationaleRequired } -> BundleAction.Request
    else -> BundleAction.Settings
}

/**
 * One action requests all runtime permissions. A multi-permission carousel starts with a feature
 * overview, then shows each permission. Each page has a hero, a horizontally scrollable icon
 * selector, and a description. Selecting an icon scrolls to that permission's page; the overview
 * leaves all icons at equal size. Android can show multiple dialogs and return partial grants.
 *
 * [onRequest] should launch one multiple-permission request. [onOpenSettings] should open app
 * settings if no outstanding permission can prompt. [initialPage] is zero for the overview when
 * there are multiple permissions, otherwise zero for the sole permission.
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
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.size == statuses.size) { "Each permission needs one status" }
    require(permissions.map { it.permission }.distinct().size == permissions.size) { "Permissions must be unique" }
    val hasOverview = permissions.size > 1
    val pageCount = permissions.size + if (hasOverview) 1 else 0
    require(initialPage in 0 until pageCount) { "initialPage is out of range" }
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val action = bundleAction(statuses)
    val current = pager.currentPage.coerceIn(0 until pageCount)

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.permission_back))
                }
                Text(stringResource(R.string.permission_progress, current + 1, pageCount),
                    style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.size(44.dp))
            }
            HorizontalPager(
                state = pager,
                key = { if (hasOverview && it == 0) "overview" else permissions[it - if (hasOverview) 1 else 0].permission },
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                val index = page - if (hasOverview) 1 else 0
                val item = permissions.getOrNull(index)
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
                        when {
                            item?.hero != null -> item.hero.invoke()
                            item == null && overview.hero != null -> overview.hero.invoke()
                            else -> DefaultBundleHero(item?.icon ?: Icons.Default.Lock)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PermissionIconStrip(permissions, statuses, index) { selected ->
                        scope.launch { pager.animateScrollToPage(selected + if (hasOverview) 1 else 0) }
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
                    if (item != null && statuses[index] == PermissionStatus.Granted) {
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.permission_bundle_granted),
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Button(
                onClick = {
                    when (action) {
                        BundleAction.Request -> onRequest()
                        BundleAction.Settings -> onOpenSettings()
                        BundleAction.Complete -> onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
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
            TextButton(onClick = onNotNow, modifier = Modifier.fillMaxWidth()) {
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
    val scroll = rememberScrollState()
    LaunchedEffect(selectedIndex) {
        scroll.animateScrollTo(if (selectedIndex < 0) 0 else selectedIndex * 64)
    }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scroll),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                        if (statuses[index] == PermissionStatus.Granted) Icons.Default.Check else item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(if (selected) 27.dp else 22.dp),
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
 * after a button tap. [overview] customizes the opening page for multiple permissions. Each
 * [PermissionDescription] accepts a custom `description` composable and a separate `hero` slot.
 * [content] is shown only while all permissions are granted. A callback result can be partial;
 * permission status is checked again on recomposition and return from Settings.
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
        PermissionBundleScreen(
            permissions = permissions,
            statuses = statuses,
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
            modifier = modifier,
        )
    }
}
