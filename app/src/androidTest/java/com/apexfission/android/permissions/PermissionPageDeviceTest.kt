package com.apexfission.android.permissions

import android.Manifest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionPageDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun individualScreenRendersHostPageAndOwnsActionLabel() {
        compose.setContent {
            MaterialTheme {
                PermissionScreen(
                    permissions = listOf(PermissionDescription(Manifest.permission.CAMERA) {
                        Text("Document scanner explanation")
                    }),
                    statuses = listOf(PermissionStatus.NotRequested),
                    onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                    requestActionLabel = "Continue with camera",
                )
            }
        }

        compose.onNodeWithText("Document scanner explanation").assertExists()
        compose.onNodeWithText("Continue with camera").assertExists()
    }
}
