package com.apexfission.android.permission

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** The separate one-button bundle concept, independent of the per-page flow. */
class PermissionBundleScreenshotTest {
    private val permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
            title = "Scan a document",
            body = "We use the camera when you choose to scan a document.",
        ),
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
        ),
    )

    @PreviewTest
    @Preview(name = "Bundle first page", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun firstPage() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }

    @PreviewTest
    @Preview(name = "Bundle second page", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun secondPage() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 1,
            )
        }
    }

    @PreviewTest
    @Preview(name = "Bundle partial denial", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun partialDenial() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.Granted, PermissionStatus.PermanentlyDenied),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 1,
            )
        }
    }
}
