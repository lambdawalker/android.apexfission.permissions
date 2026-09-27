package com.apexfission.android.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.apexfission.android.permission.DefaultDescription
import com.apexfission.android.permission.HandlePermissions
import com.apexfission.android.permission.PermissionDescription
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
                                hero = { Icon(Icons.Default.PhotoCamera, null, Modifier.size(96.dp),
                                    tint = MaterialTheme.colorScheme.primary) },
                            ) {
                                DefaultDescription(
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
                        overview = PermissionOverview(
                            title = "Scan with camera and microphone",
                            body = "Review the access this feature uses, then request both permissions together.",
                            hero = { Icon(Icons.Default.Lock, null, Modifier.size(96.dp),
                                tint = MaterialTheme.colorScheme.primary) },
                        ),
                    ) {
                        Greeting(name = "Permissions granted")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidpermissionsTheme {
        Greeting("Android")
    }
}
