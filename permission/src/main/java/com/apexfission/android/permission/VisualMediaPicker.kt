package com.apexfission.android.permission

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/** Content types offered by the system photo picker. */
enum class VisualMediaSelection { Image, Video, ImageOrVideo }

/** User-driven visual media selection; no runtime storage permission is requested. */
class VisualMediaPicker internal constructor(
    private val launchPicker: (VisualMediaSelection) -> Unit,
) {
    /** Show the platform or AndroidX-backed picker. A cancellation reports `null` to [rememberVisualMediaPicker]. */
    fun launch(selection: VisualMediaSelection = VisualMediaSelection.ImageOrVideo) = launchPicker(selection)
}

/**
 * Remembers a single-item photo/video picker. Invoke [VisualMediaPicker.launch] after a user
 * action; [onResult] receives a selected URI or `null` if the user cancels. The host owns any
 * URI persistence it needs. For a full gallery with broad media access, use a separate flow.
 */
@Composable
fun rememberVisualMediaPicker(onResult: (Uri?) -> Unit): VisualMediaPicker {
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        currentOnResult(uri)
    }
    return remember(launcher) {
        VisualMediaPicker { selection ->
            val type = when (selection) {
                VisualMediaSelection.Image -> ActivityResultContracts.PickVisualMedia.ImageOnly
                VisualMediaSelection.Video -> ActivityResultContracts.PickVisualMedia.VideoOnly
                VisualMediaSelection.ImageOrVideo -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
            }
            launcher.launch(PickVisualMediaRequest(type))
        }
    }
}
