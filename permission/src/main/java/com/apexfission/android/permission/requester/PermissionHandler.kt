package com.apexfission.android.permission.requester

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.recipe.resolvePermissionStatus
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

/**
 * Protects [content] until every required Android runtime permission is granted. One button
 * requests missing permissions together; each [PermissionDescription] supplies a complete page.
 * The host must declare every requested permission in its manifest.
 *
 * [overview] defaults to a generic lock-icon page; supply a custom page to replace it.
 * Automatic [overviewMode] shows the overview when more than one permission is visible;
 * Hide omits it.
 * [displayMode] filters only the carousel; grant checks and batch requests use the full list.
 * [onPermissionsResult] can report partial grants; the current [PermissionGrants] snapshot is
 * checked again before showing [content]. [recoveryContent], [settingsActionLabel], and
 * [onOpenSettings] customize inferred denial recovery.
 *
 * Special app access and permissions requiring staged requests need host-managed flows.
 */
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
                history.edit { putBoolean(name, false) }
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
                history.edit { putBoolean(name, false) }
            }
        }
    }
    val grants = PermissionGrants(permissions, statuses)
    if (grants.canProceed) {
        content(grants)
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
                        history.edit { putBoolean(name, true) }
                    }
                    state.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    if (onOpenSettings != null) onOpenSettings()
                    else context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                recoveryContent = recoveryContent,
                settingsActionLabel = settingsActionLabel,
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
