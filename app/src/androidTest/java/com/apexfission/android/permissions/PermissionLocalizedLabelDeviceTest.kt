package com.apexfission.android.permissions

import android.Manifest
import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.ui.PermissionBundleScreen
import com.apexfission.android.permission.ui.PermissionDescription
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionLocalizedLabelDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun omittedLabelsFollowLocaleChangesWhileExplicitLabelsRemainExact() {
        val locale = mutableStateOf("de")
        // Construct once: locale changes must be resolved when the selector renders.
        val descriptions = listOf(
            PermissionDescription(Manifest.permission.CAMERA) { Text("Host camera page") },
            PermissionDescription(Manifest.permission.RECORD_AUDIO) { Text("Host audio page") },
            PermissionDescription(Manifest.permission.READ_MEDIA_IMAGES, label = "Camera") {
                Text("Host photo page")
            },
        )
        compose.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag(locale.value))
            }
            val context = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides configuration,
            ) {
                MaterialTheme {
                    PermissionBundleScreen(
                        permissions = descriptions,
                        statuses = List(descriptions.size) { PermissionStatus.NotRequested },
                        onBack = {}, onNotNow = {}, onRequest = {}, onOpenSettings = {},
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Kamera").assertExists()
        compose.onNodeWithContentDescription("Mikrofon").assertExists()
        compose.onNodeWithContentDescription("Camera").assertExists()

        compose.runOnIdle { locale.value = "ja" }

        compose.onNodeWithContentDescription("カメラ").assertExists()
        compose.onNodeWithContentDescription("マイク").assertExists()
        compose.onNodeWithContentDescription("Camera").assertExists()
    }
}
