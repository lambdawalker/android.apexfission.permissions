package com.apexfission.android.permissions.demo

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.requester.HandlePermissions
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionDisplayMode
import com.apexfission.android.permission.ui.PermissionOverview
import com.apexfission.android.permission.ui.ReadingPace
import com.apexfission.android.permission.ui.estimateReadingDelayMillis
import com.apexfission.android.permission.recipe.openPermissionRecipeSettings
import com.apexfission.android.permissions.ui.theme.AndroidpermissionsTheme

private const val CAMERA_TITLE = "Scan documents"
private const val CAMERA_BODY = "Allow camera access to capture a document when you start a scan."
private const val MICROPHONE_TITLE = "Record narration"
private const val MICROPHONE_BODY = "Allow microphone access to add narration when you record a scan."
private const val OVERVIEW_TITLE = "Scan with camera and microphone"
private const val OVERVIEW_BODY = "Camera access is needed to scan. Microphone access adds optional narration."

@Composable
fun PermissionCarouselDemo(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    HandlePermissions(
        permissions = listOf(
            PermissionDescription(
                permission = Manifest.permission.CAMERA,
                label = "Camera",
                icon = Icons.Default.PhotoCamera,
                autoAdvanceDelayMillis = estimateReadingDelayMillis(
                    "$CAMERA_TITLE $CAMERA_BODY", ReadingPace.Slow
                ),
            ) {
                DefaultPermissionPage(
                    label = "Camera", icon = Icons.Default.PhotoCamera,
                    title = CAMERA_TITLE, body = CAMERA_BODY,
                )
            },
            PermissionDescription(
                permission = Manifest.permission.RECORD_AUDIO,
                label = "Microphone",
                icon = Icons.Default.Mic,
                required = false,
                autoAdvanceDelayMillis = estimateReadingDelayMillis(
                    "$MICROPHONE_TITLE $MICROPHONE_BODY", ReadingPace.Slow
                ),
            ) {
                DefaultPermissionPage(
                    label = "Microphone", icon = Icons.Default.Mic,
                    title = MICROPHONE_TITLE, body = MICROPHONE_BODY,
                )
            },
        ),
        modifier = modifier,
        onBack = onBack,
        onNotNow = onBack,
        displayMode = PermissionDisplayMode.All,
        autoAdvance = true,
        recoveryContent = { inferred ->
            Text(
                "Android may not show another request for ${if (inferred.size == 1) "this permission" else "these permissions"}. " +
                    "If it doesn't, review the app's access in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        settingsActionLabel = "Review access in Settings",
        onOpenSettings = { context.openPermissionRecipeSettings() },
        overview = PermissionOverview(
            autoAdvanceDelayMillis = estimateReadingDelayMillis(
                "$OVERVIEW_TITLE $OVERVIEW_BODY", ReadingPace.Slow
            ),
            page = {
                DefaultPermissionPage(
                    label = "Permissions", icon = Icons.Default.Lock,
                    title = OVERVIEW_TITLE, body = OVERVIEW_BODY,
                )
            },
        ),
    ) { grants ->
        PermissionsReady(
            microphoneGranted = grants.isGranted(Manifest.permission.RECORD_AUDIO),
            modifier = modifier,
        )
    }
}

@Composable
private fun PermissionsReady(microphoneGranted: Boolean = true, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.size(112.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text("You're ready to go", style = MaterialTheme.typography.headlineMedium)
            Text(
                if (microphoneGranted) "Camera and narration are ready. You can start scanning."
                else "Camera is ready. You can scan without narration.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Access enabled", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Camera · Capture documents")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(if (microphoneGranted) "Microphone · Narration available"
                            else "Microphone · Optional narration unavailable")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsReadyPreview() {
    AndroidpermissionsTheme {
        PermissionsReady()
    }
}
