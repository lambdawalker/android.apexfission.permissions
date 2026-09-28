package com.apexfission.android.permissions

import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionBundleScreen
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionOverviewMode
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

    @Test fun multiplePermissionsStartOnFirstPageWithoutAnOverview() {
        compose.setContent {
            MaterialTheme {
                PermissionBundleScreen(
                    permissions = listOf(
                        PermissionDescription(Manifest.permission.CAMERA) { Text("Camera explanation") },
                        PermissionDescription(Manifest.permission.RECORD_AUDIO) { Text("Audio explanation") },
                    ),
                    statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                    onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                    overviewMode = PermissionOverviewMode.Hide,
                )
            }
        }

        compose.onNodeWithText("1 of 2").assertExists()
        compose.onNodeWithText("Camera explanation").assertExists()
    }

    @Test fun multiplePermissionsShowGenericOverviewByDefault() {
        compose.setContent {
            MaterialTheme {
                PermissionBundleScreen(
                    permissions = listOf(
                        PermissionDescription(Manifest.permission.CAMERA) { Text("Camera explanation") },
                        PermissionDescription(Manifest.permission.RECORD_AUDIO) { Text("Audio explanation") },
                    ),
                    statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.NotRequested),
                    onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                )
            }
        }

        compose.onNodeWithText("1 of 3").assertExists()
        compose.onNodeWithText("Before you continue").assertExists()
        compose.onNodeWithText("The app needs some permissions to work for you.").assertExists()
    }

    @Test fun defaultPageAcceptsBitmapDrawableAndDrawableResourceHeroes() {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        compose.setContent {
            MaterialTheme {
                Column {
                    DefaultPermissionPage(label = "Bitmap", heroImage = bitmap, title = "Bitmap hero")
                    DefaultPermissionPage(label = "Drawable", heroImage = ColorDrawable(android.graphics.Color.BLUE), title = "Drawable hero")
                    DefaultPermissionPage(label = "Resource", heroImage = android.R.drawable.ic_menu_camera, title = "Resource hero")
                }
            }
        }
        compose.onNodeWithText("Bitmap hero").assertExists()
        compose.onNodeWithText("Drawable hero").assertExists()
        compose.onNodeWithText("Resource hero").assertExists()
    }
}
