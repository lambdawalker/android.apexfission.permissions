package com.apexfission.android.permissions

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode

/** Find catalog items even when the shared lazy screen has not composed them yet. */
internal fun ComposeContentTestRule.scrollToDemo(label: String): SemanticsNodeInteraction {
    onNode(hasScrollAction()).performScrollToNode(hasText(label))
    return onNodeWithText(label)
}
