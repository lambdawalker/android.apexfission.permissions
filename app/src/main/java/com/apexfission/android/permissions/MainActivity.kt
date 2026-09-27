package com.apexfission.android.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.apexfission.android.permission.DefaultPermissionPage
import com.apexfission.android.permission.HandlePermissions
import com.apexfission.android.permission.PermissionDescription
import com.apexfission.android.permission.PermissionDisplayMode
import com.apexfission.android.permission.PermissionOverview
import com.apexfission.android.permissions.ui.theme.AndroidpermissionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidpermissionsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HandlePermissions(
                        permissions = listOf(
                            PermissionDescription(
                                permission = Manifest.permission.CAMERA,
                                label = "Camera",
                                icon = Icons.Default.PhotoCamera,
                            ) {
                                DefaultPermissionPage(
                                    label = "Camera", icon = Icons.Default.PhotoCamera,
                                    title = "Scan documents",
                                    body = "Allow camera access to capture a document when you start a scan.",
                                )
                            },
                            PermissionDescription(
                                permission = Manifest.permission.RECORD_AUDIO,
                                label = "Microphone",
                                icon = Icons.Default.Mic,
                            ),
                        ),
                        modifier = Modifier.padding(innerPadding),
                        onBack = { finish() },
                        onNotNow = { finish() },
                        displayMode = PermissionDisplayMode.All,
                        overview = PermissionOverview(
                            page = {
                                DefaultPermissionPage(
                                    label = "Permissions", icon = Icons.Default.Lock,
                                    title = "Scan with camera and microphone",
                                    body = "Review the access this feature uses, then request both permissions together.",
                                )
                            },
                        ),
                    ) {
                        PermissionsReady(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsReady(modifier: Modifier = Modifier) {
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
                "Camera and microphone access are available. Your app can now start the protected feature.",
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
                        Text("Microphone · Record audio")
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
