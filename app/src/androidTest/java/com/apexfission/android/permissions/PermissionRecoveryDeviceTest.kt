package com.apexfission.android.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.apexfission.android.permission.HandlePermissions
import com.apexfission.android.permission.PermissionDescription
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises a real runtime dialog, repeated denial, recovery copy, and a return from Settings. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 30)
class PermissionRecoveryDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun twoDenialsOfferUncertainRecoveryAndSettings() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        assumeTrue(context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        context.getSharedPreferences("camera_permission_state", 0).edit().clear().commit()
        val device = UiDevice.getInstance(instrumentation)

        compose.setContent {
            MaterialTheme {
                HandlePermissions(
                    permissions = listOf(PermissionDescription(Manifest.permission.CAMERA, label = "Camera")),
                    onBack = {}, onNotNow = {},
                ) { }
            }
        }
        repeat(2) {
            compose.onNodeWithText(if (it == 0) "Request permissions" else "Request remaining permissions")
                .performClick()
            val deny = device.wait(Until.findObject(By.text("Don't allow")), 10_000)
                ?: device.wait(Until.findObject(By.text("Deny")), 2_000)
            assertNotNull("Android permission dialog did not appear", deny)
            deny!!.click()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Android might not show another permission prompt.", substring = true)
            .assertExists()
        compose.onNodeWithText("Open App Settings").performClick()
        device.wait(Until.hasObject(By.pkg("com.android.settings")), 10_000)
        assertNotNull("App Settings did not open", device.findObject(By.pkg("com.android.settings")))
        device.pressBack()
        compose.onNodeWithText("Android might not show another permission prompt.", substring = true)
            .assertExists()
    }
}
