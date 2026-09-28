package com.apexfission.android.permission

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Protects [content] until every required Android runtime permission is granted. One button
 * requests required and optional permissions together; the carousel explains each grant.
 *
 * The host must declare every permission in its manifest. This gate intentionally does not handle
 * special app access (such as exact alarms) or install-time permissions, which Android requests
 * through different mechanisms.
 *
 * @param permissions Nonempty list of unique permission strings with optional per-page copy/hero.
 * @param onBack Back navigation from the explanation UI.
 * @param onNotNow Exit or defer the feature without requesting access.
 * @param permissionContent Optional host UI replacing the built-in primer; receives controllers
 * in the same order as [permissions]. Use [HandlePermissionsIndividually] when that custom UI
 * needs individual request controllers.
 * @param displayMode Whether the built-in carousel shows all permissions or only those missing.
 * The custom [permissionContent] path still receives every controller.
 * @param overviewMode Whether the built-in carousel includes a feature overview page. Automatic
 * shows it when more than one permission is visible; Show and Hide override either count.
 * @param autoAdvance Whether to advance through the carousel using each page's configured delay
 * until the first user interaction. Disabled by default.
 * @param content Composable displayed when all required permissions are granted. Its
 * [PermissionGrants] snapshot reports optional grants, including after a partial result.
 */
@Composable
fun HandlePermissions(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    permissionContent: (@Composable (List<PermissionController>) -> Unit)? = null,
    overview: PermissionOverview = PermissionOverview(),
    displayMode: PermissionDisplayMode = PermissionDisplayMode.All,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    content: @Composable (PermissionGrants) -> Unit,
) {
    if (permissionContent == null) {
        HandlePermissionBundle(
            permissions = permissions,
            onBack = onBack,
            onNotNow = onNotNow,
            modifier = modifier,
            overview = overview,
            displayMode = displayMode,
            overviewMode = overviewMode,
            autoAdvance = autoAdvance,
            content = content,
        )
        return
    }
    HandlePermissionsIndividually(
        permissions, onBack, onNotNow, modifier, permissionContent, content,
    )
}

/** Legacy per-permission request flow, retained for callers that need individual actions. */
@Composable
fun HandlePermissionsIndividually(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    permissionContent: (@Composable (List<PermissionController>) -> Unit)? = null,
    content: @Composable (PermissionGrants) -> Unit,
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.map { it.permission }.distinct().size == permissions.size) {
        "Permissions must be unique"
    }
    val controllers = permissions.map { description ->
        key(description.permission) { rememberPermissionController(description.permission) }
    }
    val statuses = controllers.map { it.status }
    val grants = PermissionGrants(permissions, statuses)
    if (grants.canProceed) {
        content(grants)
    } else if (permissionContent != null) {
        permissionContent(controllers)
    } else {
        PermissionScreen(
            permissions = permissions,
            statuses = statuses,
            onBack = onBack,
            onNotNow = onNotNow,
            onRequest = { controllers[it].requestPermission() },
            onOpenSettings = { controllers[it].openAppSettings() },
            modifier = modifier,
        )
    }
}

/**
 * Camera-only compatibility gate. New integrations should use [HandlePermissions].
 * [permissionContent] can still provide custom camera UI with explicit request/settings actions.
 */
@Composable
fun HandleCameraPermission(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    permissionViewModel: PermissionViewModel = viewModel(),
    permissionContent: (@Composable (CameraPermissionController) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val controller = rememberCameraPermissionController(permissionViewModel)
    when {
        controller.status == CameraPermissionStatus.Granted -> content()
        permissionContent != null -> permissionContent(controller)
        else -> PermissionScreen(
            permissions = listOf(
                PermissionDescription(
                    permission = Manifest.permission.CAMERA,
                    label = stringResource(R.string.camera_permission_label),
                    icon = Icons.Default.PhotoCamera,
                    title = stringResource(R.string.camera_permission_title),
                    body = stringResource(R.string.camera_permission_body),
                    requestLabel = stringResource(R.string.camera_permission_allow),
                )
            ),
            statuses = listOf(
                when (controller.status) {
                    CameraPermissionStatus.Granted -> PermissionStatus.Granted
                    CameraPermissionStatus.NotRequested -> PermissionStatus.NotRequested
                    CameraPermissionStatus.RationaleRequired -> PermissionStatus.RationaleRequired
                    CameraPermissionStatus.PermanentlyDenied -> PermissionStatus.PermanentlyDenied
                }
            ),
            onBack = onBack,
            onNotNow = onNotNow,
            onRequest = { controller.requestPermission() },
            onOpenSettings = { controller.openAppSettings() },
            modifier = modifier,
        )
    }
}
