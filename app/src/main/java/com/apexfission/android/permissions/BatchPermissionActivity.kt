package com.apexfission.android.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Text
import com.apexfission.android.permission.HandlePermissionBundle
import com.apexfission.android.permission.PermissionDescription
import com.apexfission.android.permissions.ui.theme.AndroidpermissionsTheme

/** Separate sample entry point for the one-button batch request. */
class BatchPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidpermissionsTheme {
                HandlePermissionBundle(
                    permissions = listOf(
                        PermissionDescription(
                            permission = Manifest.permission.CAMERA,
                            label = "Camera",
                            icon = Icons.Default.PhotoCamera,
                            title = "Scan documents",
                            body = "Allow camera access when you choose to scan a document.",
                        ),
                        PermissionDescription(
                            permission = Manifest.permission.RECORD_AUDIO,
                            label = "Microphone",
                            icon = Icons.Default.Mic,
                        ),
                    ),
                    onBack = { finish() },
                    onNotNow = { finish() },
                ) {
                    Text("Both permissions granted")
                }
            }
        }
    }
}
