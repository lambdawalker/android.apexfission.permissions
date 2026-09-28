package com.apexfission.android.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

/** Observable status of a runtime permission. */
enum class PermissionStatus {
    Granted, NotRequested, RationaleRequired,
    /** Inferred from local request history and Android's rationale signal; not a definitive platform flag. */
    PermanentlyDenied,
}

/** Explicit actions for one Android runtime permission. Composition never launches a request. */
@Stable
class PermissionController internal constructor(
    /** The permission string, such as `android.permission.CAMERA`. */
    val permission: String,
    /** Current status, refreshed by Accompanist as the host resumes. */
    val status: PermissionStatus,
    private val request: () -> Unit,
    private val openSettings: () -> Unit,
) {
    /** Requests this permission; call only after the user takes an explicit action. */
    fun requestPermission() = request()

    /** Opens the host application's details page in system settings. */
    fun openAppSettings() = openSettings()
}

/**
 * Remembers a runtime permission and persists request history separately for each permission.
 * The host app must declare [permission] in its manifest. This API is for runtime permissions;
 * special app access uses a separate Android settings flow.
 *
 * Android's rationale signal cannot definitively distinguish a first request from a permanent
 * denial. [PermissionStatus.PermanentlyDenied] combines that signal with local request history.
 *
 * @param permission Android runtime permission string.
 * @param permissionViewModel Optional adapter for testing or host scoping.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionController(
    permission: String,
    permissionViewModel: PermissionViewModel = viewModel(),
): PermissionController {
    require(permission.isNotBlank()) { "permission must not be blank" }
    val applicationContext = LocalContext.current.applicationContext
    val history = remember(applicationContext) {
        applicationContext.getSharedPreferences("camera_permission_state", Context.MODE_PRIVATE)
    }
    var requestedBefore by rememberSaveable(permission) {
        mutableStateOf(history.getBoolean(permission, false))
    }
    val state = permissionViewModel.rememberRuntimePermissionState(permission) { granted ->
        if (granted) {
            requestedBefore = false
            history.edit().putBoolean(permission, false).apply()
        }
    }
    val granted = state.status.isGranted
    LaunchedEffect(granted, permission) {
        if (granted && requestedBefore) {
            requestedBefore = false
            history.edit().putBoolean(permission, false).apply()
        }
    }
    return PermissionController(
        permission = permission,
        status = resolvePermissionStatus(granted, state.status.shouldShowRationale, requestedBefore),
        request = {
            requestedBefore = true
            history.edit().putBoolean(permission, true).apply()
            permissionViewModel.launchPermissionRequest(state)
        },
        openSettings = {
            applicationContext.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", applicationContext.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
    )
}

internal fun resolvePermissionStatus(
    granted: Boolean,
    shouldShowRationale: Boolean,
    requestedBefore: Boolean,
): PermissionStatus = when {
    granted -> PermissionStatus.Granted
    shouldShowRationale -> PermissionStatus.RationaleRequired
    requestedBefore -> PermissionStatus.PermanentlyDenied
    else -> PermissionStatus.NotRequested
}

/** Permissions that may require Settings, preserving the caller's order. */
internal fun inferredRecoveryPermissions(
    permissions: List<String>, statuses: List<PermissionStatus>,
): List<String> {
    require(permissions.size == statuses.size) { "Each permission needs one status" }
    return permissions.zip(statuses).filter { it.second == PermissionStatus.PermanentlyDenied }
        .map { it.first }
}

internal fun allPermissionsGranted(statuses: List<PermissionStatus>): Boolean =
    statuses.isNotEmpty() && statuses.all { it == PermissionStatus.Granted }

internal fun nextOutstandingPermission(statuses: List<PermissionStatus>, current: Int): Int? {
    if (statuses.isEmpty()) return null
    return (1..statuses.size).map { (current + it) % statuses.size }
        .firstOrNull { statuses[it] != PermissionStatus.Granted }
}

internal enum class PermissionPrimaryAction { Request, Settings, Next }

internal fun primaryAction(status: PermissionStatus): PermissionPrimaryAction = when (status) {
    PermissionStatus.NotRequested, PermissionStatus.RationaleRequired -> PermissionPrimaryAction.Request
    PermissionStatus.PermanentlyDenied -> PermissionPrimaryAction.Settings
    PermissionStatus.Granted -> PermissionPrimaryAction.Next
}
