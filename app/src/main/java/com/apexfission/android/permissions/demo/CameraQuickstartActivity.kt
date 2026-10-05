package com.apexfission.android.permissions.demo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.apexfission.android.permission.requester.HandlePermissions
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionDescription

class CameraQuickstartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HandlePermissions(
                    modifier = Modifier.safeDrawingPadding(),
                    permissions = listOf(
                        PermissionDescription(Manifest.permission.CAMERA) {
                            DefaultPermissionPage(
                                label = "Camera",
                                title = "Scan a document",
                                body = "Allow camera access to scan your document.",
                            )
                        },
                    ),
                    onBack = { finish() },
                    onNotNow = { finish() },
                ) { grants ->
                    Text("Camera access: ${grants.isGranted(Manifest.permission.CAMERA)}")
                }
            }
        }
    }
}
