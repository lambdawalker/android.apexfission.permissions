package com.apexfission.android.permission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apexfission.android.permission.recipe.PermissionController
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.recipe.rememberPermissionController
import com.apexfission.android.permission.recipe.resolvePermissionStatus
import com.apexfission.android.permission.ui.PermissionViewModel

/** Compatibility status for camera-only consumers. Prefer [com.apexfission.android.permission.recipe.PermissionStatus]. */
enum class CameraPermissionStatus { Granted, NotRequested, RationaleRequired, PermanentlyDenied }

/** Camera-only compatibility controller. Prefer [com.apexfission.android.permission.recipe.PermissionController]. */
class CameraPermissionController internal constructor(private val delegate: PermissionController) {
    /** Camera permission status. */
    val status: CameraPermissionStatus get() = delegate.status.toCameraStatus()
    /** Requests camera permission after an explicit user action. */
    fun requestPermission() = delegate.requestPermission()
    /** Opens app settings. */
    fun openAppSettings() = delegate.openAppSettings()
}

/** Camera-only compatibility API. Prefer [com.apexfission.android.permission.recipe.rememberPermissionController]. */
@Composable
fun rememberCameraPermissionController(
    permissionViewModel: PermissionViewModel = viewModel(),
): CameraPermissionController = CameraPermissionController(
    rememberPermissionController(Manifest.permission.CAMERA, permissionViewModel)
)

internal fun resolveCameraPermissionStatus(
    granted: Boolean,
    shouldShowRationale: Boolean,
    requestedBefore: Boolean,
): CameraPermissionStatus = resolvePermissionStatus(granted, shouldShowRationale, requestedBefore).toCameraStatus()

private fun PermissionStatus.toCameraStatus(): CameraPermissionStatus = when (this) {
    PermissionStatus.Granted -> CameraPermissionStatus.Granted
    PermissionStatus.NotRequested -> CameraPermissionStatus.NotRequested
    PermissionStatus.RationaleRequired -> CameraPermissionStatus.RationaleRequired
    PermissionStatus.PermanentlyDenied -> CameraPermissionStatus.PermanentlyDenied
}
