package com.apexfission.android.permissions

import android.Manifest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionBundleScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionPageDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bundleScreenRendersHostPageAndUsesOneRequestAction() {
        var requested = false
        compose.setContent {
            MaterialTheme {
                PermissionBundleScreen(
                    permissions = listOf(PermissionDescription(Manifest.permission.CAMERA) {
                        Text("Document scanner explanation")
                    }),
                    statuses = listOf(PermissionStatus.NotRequested),
                    onBack = {}, onNotNow = {}, onRequest = { requested = true }, onOpenSettings = {},
                )
            }
        }

        compose.onNodeWithText("Document scanner explanation").assertExists()
        compose.onNodeWithText("Request permissions").performClick()
        compose.runOnIdle { assertTrue(requested) }
    }
}
