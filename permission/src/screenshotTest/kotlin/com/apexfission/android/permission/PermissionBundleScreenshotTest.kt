package com.apexfission.android.permission

import android.Manifest
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** The default one-button carousel: overview, individual page, and denial recovery. */
class PermissionBundleScreenshotTest {
    private val permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
            title = "Scan a document",
            body = "We use the camera when you choose to scan a document.",
            page = {
                DefaultPermissionPage("Camera", Icons.Default.PhotoCamera,
                    title = "Scan a document",
                    body = "We use the camera when you choose to scan a document.")
            },
        ),
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
        ),
    )

    @PreviewTest
    @Preview(name = "Bundle overview", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun firstPage() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                overview = PermissionOverview(page = {
                    DefaultPermissionPage("Permissions", Icons.Default.PhotoCamera,
                        title = "Create a narrated scan",
                        body = "Review camera and microphone access before starting.")
                }),
            )
        }
    }

    @PreviewTest
    @Preview(name = "Bundle camera page", widthDp = 393, heightDp = 852, showBackground = true)
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
                initialPage = 2,
            )
        }
    }

    @PreviewTest
    @Preview(name = "Bundle scrollable icons", widthDp = 320, heightDp = 720, showBackground = true)
    @Composable
    fun scrollableIcons() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = (1..8).map { index ->
                    PermissionDescription("test.permission.$index", "Permission $index", Icons.Default.Mic)
                },
                statuses = List(8) { PermissionStatus.NotRequested },
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 8,
            )
        }
    }

    @PreviewTest
    @Preview(name = "Bundle overflow overview", widthDp = 320, heightDp = 720, showBackground = true)
    @Composable
    fun overflowOverview() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = (1..8).map { index ->
                    PermissionDescription("test.permission.$index", "Permission $index", Icons.Default.Mic)
                },
                statuses = List(8) { PermissionStatus.NotRequested },
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }

    @PreviewTest
    @Preview(name = "All with granted badge", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun allWithGrantedBadge() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.Granted, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 1,
            )
        }
    }

    @PreviewTest
    @Preview(name = "Missing only overview", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun missingOnlyOverview() {
        val all = permissions + PermissionDescription(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            label = "Location",
            icon = Icons.Default.LocationOn,
        )
        val statuses = listOf(
            PermissionStatus.Granted,
            PermissionStatus.NotRequested,
            PermissionStatus.RationaleRequired,
        )
        val visible = visiblePermissionIndices(statuses, PermissionDisplayMode.MissingOnly)
        MaterialTheme {
            PermissionBundleScreen(
                permissions = visible.map(all::get),
                statuses = visible.map(statuses::get),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }

    @PreviewTest
    @Preview(name = "Single missing permission", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun singleMissingPermission() {
        val statuses = listOf(PermissionStatus.Granted, PermissionStatus.NotRequested)
        val visible = visiblePermissionIndices(statuses, PermissionDisplayMode.MissingOnly)
        MaterialTheme {
            PermissionBundleScreen(
                permissions = visible.map(permissions::get),
                statuses = visible.map(statuses::get),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }

    @PreviewTest
    @Preview(name = "Granted badge dark", widthDp = 393, heightDp = 852,
        uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
    @Composable
    fun grantedBadgeDark() {
        MaterialTheme(colorScheme = darkColorScheme()) {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.Granted, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 1,
            )
        }
    }
}
