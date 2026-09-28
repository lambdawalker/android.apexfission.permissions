package com.apexfission.android.permission

import android.Manifest
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionBundleScreen
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionDisplayMode
import com.apexfission.android.permission.ui.PermissionOverview
import com.apexfission.android.permission.ui.PermissionOverviewMode
import com.apexfission.android.permission.ui.visiblePermissionIndices

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
                DefaultPermissionPage(
                    "Camera", Icons.Default.PhotoCamera,
                    title = "Scan a document",
                    body = "We use the camera when you choose to scan a document."
                )
            },
        ),
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
        ),
    )

    @PreviewTest
    @Preview(name = "Autoplay reading progress", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun autoplayReadingProgress() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                autoAdvance = true,
                initialPage = 1, // overview segment complete, camera segment in progress
            )
        }
    }

    @PreviewTest
    @Preview(name = "Optional microphone", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun optionalMicrophone() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = listOf(
                    PermissionDescription(Manifest.permission.CAMERA, label = "Camera", icon = Icons.Default.PhotoCamera),
                    PermissionDescription(
                        Manifest.permission.RECORD_AUDIO, label = "Microphone",
                        icon = Icons.Default.Mic, required = false
                    ),
                ),
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 2,
            )
        }
    }

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
                    DefaultPermissionPage(
                        "Permissions", Icons.Default.PhotoCamera,
                        title = "Create a narrated scan",
                        body = "Review camera and microphone access before starting."
                    )
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
    @Preview(name = "Custom recovery", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun customRecovery() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.Granted, PermissionStatus.PermanentlyDenied),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 2,
                recoveryContent = { inferred -> Text("Access may need Settings: ${inferred.size} permission") },
                settingsActionLabel = "Review access",
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
    @Preview(name = "Single permission overview", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun singlePermissionOverview() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions.take(1),
                statuses = listOf(PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                overviewMode = PermissionOverviewMode.Show,
                overview = PermissionOverview(title = "Before scanning", body = "Review camera access first."),
            )
        }
    }

    @PreviewTest
    @Preview(name = "Multiple permissions no overview", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun multiplePermissionsNoOverview() {
        MaterialTheme {
            PermissionBundleScreen(
                permissions = permissions,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                overviewMode = PermissionOverviewMode.Hide,
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
