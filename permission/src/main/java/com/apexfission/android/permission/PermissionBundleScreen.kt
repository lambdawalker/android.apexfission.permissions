package com.apexfission.android.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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

/** Decision for the one primary button on [PermissionBundleScreen]. */
internal enum class BundleAction { Request, Settings, Complete }

internal fun bundleAction(statuses: List<PermissionStatus>): BundleAction = when {
    allPermissionsGranted(statuses) -> BundleAction.Complete
    statuses.any { it == PermissionStatus.NotRequested || it == PermissionStatus.RationaleRequired } -> BundleAction.Request
    else -> BundleAction.Settings
}

/**
 * A separate screen that explains each permission in a carousel and offers one batch action.
 * Android may still show more than one system dialog and can grant only some permissions.
 * This composable has no permission launcher; [onRequest] is called from the button.
 *
 * @param permissions Nonempty list of unique runtime permission descriptions.
 * @param statuses Statuses in the same order as [permissions].
 * @param onRequest Launch one request for the bundle of missing runtime permissions.
 * @param onOpenSettings Open host app settings when every missing permission cannot prompt.
 * @param onComplete Called when all are granted in a standalone use of this screen.
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
    onComplete: () -> Unit = {},
    initialPage: Int = 0,
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.size == statuses.size) { "Each permission needs one status" }
    require(permissions.map { it.permission }.distinct().size == permissions.size) { "Permissions must be unique" }
    require(initialPage in permissions.indices) { "initialPage is out of range" }
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { permissions.size })
    val scope = rememberCoroutineScope()
    val action = bundleAction(statuses)
    val selected = pager.currentPage.coerceIn(permissions.indices)

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
                Text(stringResource(R.string.permission_progress, selected + 1, permissions.size),
                    style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.size(44.dp))
            }
            Text(stringResource(R.string.permission_bundle_heading), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (permissions.size > 1) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    permissions.forEachIndexed { index, item ->
                        val size by animateDpAsState(if (index == selected) 56.dp else 44.dp,
                            label = "bundle icon size")
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp).size(56.dp)
                                .semantics { contentDescription = item.label }
                                .clickable { scope.launch { pager.animateScrollToPage(index) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier.size(size).background(
                                    if (index == selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape,
                                ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (statuses[index] == PermissionStatus.Granted) Icons.Default.Check else item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (index == selected) 27.dp else 22.dp),
                                    tint = if (index == selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            HorizontalPager(
                state = pager,
                key = { permissions[it].permission },
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                val item = permissions[page]
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
                        if (item.hero != null) item.hero.invoke() else Box(
                            modifier = Modifier.size(width = 260.dp, height = 200.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(item.icon, contentDescription = null, modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        item.title ?: stringResource(R.string.permission_generic_title, item.label),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        item.body ?: stringResource(R.string.permission_generic_body, item.label),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    if (statuses[page] == PermissionStatus.Granted) {
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.permission_bundle_granted),
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    if (item.features.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        item.features.forEach { feature ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(feature.icon, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Column(Modifier.padding(start = 12.dp)) {
                                    Text(feature.title, fontWeight = FontWeight.SemiBold)
                                    Text(feature.subtitle, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
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

/**
 * Batch-request alternative to [HandlePermissions]. One button launches Android's multiple
 * permission request, then the UI reflects each grant or denial. The existing per-page gate is
 * unchanged. The host must declare every permission in the manifest. Special app access is not
 * supported by Android's runtime permission launcher.
 *
 * @param onPermissionsResult Raw per-permission results from the batch request. The map can
 * contain partial grants; always check the current status again when using a protected feature.
 * @param content Shown only after all permissions are granted.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissionBundle(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
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
            requestedBefore = requested[permission.permission] == true,
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
            modifier = modifier,
        )
    }
}
