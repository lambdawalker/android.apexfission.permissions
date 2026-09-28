package com.apexfission.android.permissions

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Launches both demo destinations in an Activity on the emulator. */
@RunWith(AndroidJUnit4::class)
class DemoNavigationDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun launcherOpensRecipesAndCarousel() {
        compose.onNodeWithText("Open recipes").performClick()
        compose.onNodeWithText("Approximate location").assertExists()
        compose.onNodeWithText("Background location").assertExists()
        compose.onNodeWithText("Choose media").assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Approximate location").assertExists()
        compose.onNodeWithText("Back to demos").performClick()
        compose.onNodeWithText("Open carousel").performClick()
        compose.onNodeWithText("Scan with camera and microphone").assertExists()
    }
}
