package com.apexfission.android.permissions

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive user journey test set covering end-to-end app flows:
 * - App launch and home screen navigation
 * - Permission carousel demo navigation, auto-advance pausing, and page inspection
 * - Platform recipes demo navigation and card verification
 * - Activity recreation and full round-trip journey back and forth
 */
@RunWith(AndroidJUnit4::class)
class AppUserJourneyDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @After
    fun tearDown() {
        compose.mainClock.autoAdvance = true
    }

    @Test
    fun homeToCarouselAndBackJourney() {
        // 1. Verify Home screen
        compose.onNodeWithText("Permission demos").assertExists()
        compose.onNodeWithText("Permission carousel").assertExists()

        // 2. Navigate to Carousel demo
        // The carousel loops its timer; keep the test clock paused for a stable first page.
        compose.onNodeWithText("Open carousel").performScrollTo()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Open carousel").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Scan with camera and microphone").assertExists()

        // 3. Check page details
        compose.onNodeWithText("1 of 3").assertExists()
        compose.onNodeWithText("Camera access is needed to scan. Microphone access adds optional narration.").assertExists()

        // 4. Recreate activity during carousel journey
        compose.activityRule.scenario.recreate()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Scan with camera and microphone").assertExists()

        // 5. Navigate back to home
        compose.onNodeWithContentDescription("Back").performClick()
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("Permission demos").assertExists()
    }

    @Test
    fun homeToPlatformRecipesAndBackJourney() {
        // 1. Verify Home screen
        compose.onNodeWithText("Permission demos").assertExists()

        // 2. Navigate to Platform recipes demo
        compose.onNodeWithText("Open recipes").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Back to demos").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Back to demos").assertExists()
        compose.onNodeWithText("Approximate location").performScrollTo().assertExists()
        compose.onNodeWithText("Precise location").performScrollTo().assertExists()
        compose.onNodeWithText("Background location").performScrollTo().assertExists()
        compose.onNodeWithText("Notifications").performScrollTo().assertExists()
        compose.onNodeWithText("Choose media").performScrollTo().assertExists()

        // 3. Recreate activity during recipes journey
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Back to demos").assertExists()
        compose.onNodeWithText("Approximate location").performScrollTo().assertExists()

        // 4. Return to home
        compose.onNodeWithText("Back to demos").performScrollTo().performClick()
        compose.onNodeWithText("Permission demos").assertExists()
    }

    @Test
    fun fullAppRoundTripJourneyTest() {
        // Visit Carousel
        compose.onNodeWithText("Open carousel").performScrollTo()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Open carousel").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Scan with camera and microphone").assertExists()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("Permission demos").assertExists()

        // Visit Recipes
        compose.onNodeWithText("Open recipes").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Back to demos").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Back to demos").performScrollTo().performClick()
        compose.onNodeWithText("Permission demos").assertExists()
    }
}
