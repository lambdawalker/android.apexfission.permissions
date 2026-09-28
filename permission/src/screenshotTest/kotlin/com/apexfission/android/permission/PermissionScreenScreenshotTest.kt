package com.apexfission.android.permission

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.android.tools.screenshot.PreviewTest
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionScreen

/** Static snapshots for generic, customized, and multiple permission pages. */
class PermissionScreenScreenshotTest {
    private val items = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",
            icon = Icons.Default.PhotoCamera,
        ) {
            Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(190.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PhotoCamera, null, Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Scan a document", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text("We use the camera when you choose to scan a document.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        },
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
            label = "Microphone",
            icon = Icons.Default.Mic,
        ) { DefaultPermissionPage("Microphone", Icons.Default.Mic) },
    )

    @PreviewTest
    @Preview(name = "Single generic", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun singleGeneric() {
        MaterialTheme {
            PermissionScreen(
                permissions = listOf(items[1]),
                statuses = listOf(PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }

    @PreviewTest
    @Preview(name = "Multiple custom hero", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun multipleCustomHero() {
        MaterialTheme {
            PermissionScreen(
                permissions = items,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.RationaleRequired),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }

    @PreviewTest
    @Preview(name = "Multiple second page", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun multipleSecondPage() {
        MaterialTheme {
            PermissionScreen(
                permissions = items,
                statuses = listOf(PermissionStatus.Granted, PermissionStatus.PermanentlyDenied),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                initialPage = 1,
            )
        }
    }

    @PreviewTest
    @Preview(name = "Dark theme", widthDp = 393, heightDp = 852,
        uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
    @Composable
    fun darkTheme() {
        MaterialTheme(colorScheme = darkColorScheme()) {
            PermissionScreen(
                permissions = items,
                statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
            )
        }
    }
}
