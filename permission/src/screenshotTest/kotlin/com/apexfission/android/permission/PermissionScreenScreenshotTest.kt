package com.apexfission.android.permission

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** Static render fixtures for the visual states of the camera permission primer. */
class PermissionScreenScreenshotTest {

    @PreviewTest
    @Preview(name = "Initial request", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun initialRequest() {
        MaterialTheme {
            PermissionScreen(onBack = {}, onAllow = {}, onNotNow = {})
        }
    }

    @PreviewTest
    @Preview(name = "Settings recovery", widthDp = 393, heightDp = 852, showBackground = true)
    @Composable
    fun settingsRecovery() {
        MaterialTheme {
            PermissionScreen(
                onBack = {},
                onAllow = {},
                onNotNow = {},
                primaryActionText = stringResource(R.string.camera_permission_open_settings),
            )
        }
    }

    @PreviewTest
    @Preview(
        name = "Dark theme",
        widthDp = 393,
        heightDp = 852,
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
    )
    @Composable
    fun darkTheme() {
        MaterialTheme(colorScheme = darkColorScheme()) {
            PermissionScreen(onBack = {}, onAllow = {}, onNotNow = {})
        }
    }
}
