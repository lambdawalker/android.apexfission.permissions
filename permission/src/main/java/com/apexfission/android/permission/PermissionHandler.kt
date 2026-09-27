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
 * Protects [content] until every listed Android runtime permission is granted. One button
 * requests the permissions together; the carousel provides an overview and individual detail.
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
 * @param content Composable displayed only when all listed permissions are granted.
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
    content: @Composable () -> Unit,
) {
    if (permissionContent == null) {
        HandlePermissionBundle(
            permissions = permissions,
            onBack = onBack,
            onNotNow = onNotNow,
            modifier = modifier,
            overview = overview,
            displayMode = displayMode,
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
    content: @Composable () -> Unit,
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.map { it.permission }.distinct().size == permissions.size) {
        "Permissions must be unique"
    }
    val controllers = permissions.map { description ->
        key(description.permission) { rememberPermissionController(description.permission) }
    }
    if (allPermissionsGranted(controllers.map { it.status })) {
        content()
    } else if (permissionContent != null) {
        permissionContent(controllers)
    } else {
        PermissionScreen(
            permissions = permissions,
            statuses = controllers.map { it.status },
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
