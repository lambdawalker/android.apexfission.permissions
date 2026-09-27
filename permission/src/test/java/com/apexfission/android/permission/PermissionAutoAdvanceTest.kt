package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PermissionAutoAdvanceTest {
    @Test fun `reading time grows with word count and slower pace`() {
        val copy = List(20) { "word" }.joinToString(" ")
        assertEquals(12_000L, estimateReadingDelayMillis(copy, ReadingPace.Slow))
        assertEquals(8_666L, estimateReadingDelayMillis(copy, ReadingPace.Normal))
        assertEquals(7_217L, estimateReadingDelayMillis(copy, ReadingPace.Fast))
    }

    @Test fun `empty and whitespace copy gets minimum time`() {
        assertEquals(4_000L, estimateReadingDelayMillis(""))
        assertEquals(4_000L, estimateReadingDelayMillis("   \n  "))
        assertEquals(4_000L, estimateReadingDelayMillis("one"))
    }

    @Test fun `very long copy is bounded`() {
        assertEquals(60_000L, estimateReadingDelayMillis(List(500) { "word" }.joinToString(" ")))
    }

    @Test fun `autoplay loops from the final page back to the first`() {
        assertEquals(1, nextAutoAdvancePage(0, 3))
        assertEquals(2, nextAutoAdvancePage(1, 3))
        assertEquals(0, nextAutoAdvancePage(2, 3))
        assertEquals(0, nextAutoAdvancePage(1, 2))
        assertNull(nextAutoAdvancePage(0, 1))
    }

    @Test fun `resuming a paused page waits only for its remaining reading time`() {
        assertEquals(6_000, remainingAutoAdvanceMillis(0f, 6_000L))
        assertEquals(3_000, remainingAutoAdvanceMillis(0.5f, 6_000L))
        assertEquals(1, remainingAutoAdvanceMillis(1f, 6_000L))
    }
}
