package com.apexfission.android.permissions

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Launches all demo destinations in an Activity on the emulator. */
@RunWith(AndroidJUnit4::class)
class DemoNavigationDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun launcherOpensRecipesAndCarousel() {
        compose.scrollToDemo("Open recipes").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Back to demos").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Back to demos").assertExists()
        compose.onNodeWithText("Choose media").performScrollTo().assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Back to demos").assertExists()
        compose.onNodeWithText("Back to demos").performScrollTo().performClick()
        compose.scrollToDemo("Open carousel")
        // The carousel loops its timer; keep the test clock paused for a stable first page.
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Open carousel").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Scan with camera and microphone").assertExists()
    }

    @Test fun launcherOpensCodeOnlyCallbacks() {
        compose.scrollToDemo("Open callbacks").performClick()
        compose.onNodeWithText("Code-only requests").assertExists()
        compose.onNodeWithText("Check with your own launcher").assertExists()
        compose.onNodeWithText("Use the library requester").assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Code-only requests").assertExists()
    }

    @Test fun catalogSettingsActionOpensAppDetails() {
        val label = compose.activity.getString(
            com.apexfission.android.democommon.R.string.demo_common_app_settings
        )
        compose.scrollToDemo(label).performClick()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertNotNull("App Settings did not open",
            device.wait(Until.findObject(By.pkg("com.android.settings")), 10_000))
        device.pressBack()
        // Returning from Settings keeps the catalog navigable, including lazily composed items.
        compose.scrollToDemo("Open callbacks").performClick()
        compose.onNodeWithText("Code-only requests").assertExists()
    }

    @Test fun launcherOpensArtworkGallery() {
        compose.scrollToDemo("Open artwork gallery").performClick()
        compose.onNodeWithText("Hero artwork gallery").assertExists()
        compose.onNodeWithText("Bitmap hero").performScrollTo().assertExists()
        compose.onNodeWithText("Drawable resource hero").performScrollTo().assertExists()
    }
}
