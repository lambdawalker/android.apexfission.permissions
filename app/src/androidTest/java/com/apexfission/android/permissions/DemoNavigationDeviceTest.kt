package com.apexfission.android.permissions

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Launches both demo destinations in an Activity on the emulator. */
@RunWith(AndroidJUnit4::class)
class DemoNavigationDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun launcherOpensRecipesAndCarousel() {
        compose.onNodeWithText("Open recipes").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Back to demos").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Back to demos").assertExists()
        compose.onNodeWithText("Choose media").performScrollTo().assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Back to demos").assertExists()
        compose.onNodeWithText("Back to demos").performScrollTo().performClick()
        // The carousel loops its timer; keep the test clock paused for a stable first page.
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Open carousel").performScrollTo().performClick()
        compose.onNodeWithText("Scan with camera and microphone").assertExists()
    }
}
