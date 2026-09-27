package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PermissionCheckTest {
    @Test fun `runs protected action once when all grants exist`() {
        var actions = 0
        var missingCalls = 0
        val result = evaluatePermissionCheck(listOf("camera", "audio", "camera"), { true }) {
            actions++
        }.otherwise { missingCalls++ }

        assertEquals(emptyList<String>(), result.missing)
        assertEquals(1, actions)
        assertEquals(0, missingCalls)
    }

    @Test fun `returns only missing unique permissions without running action`() {
        var actions = 0
        var requested: List<String>? = null
        val result = evaluatePermissionCheck(
            listOf("camera", "audio", "audio", "gps"),
            isGranted = { it == "camera" },
        ) { actions++ }.otherwise { requested = it }

        assertEquals(listOf("audio", "gps"), result.missing)
        assertEquals(result.missing, requested)
        assertEquals(0, actions)
    }

    @Test fun `requires a real permission list`() {
        assertThrows(IllegalArgumentException::class.java) {
            evaluatePermissionCheck(emptyList(), { true }) {}
        }
        assertThrows(IllegalArgumentException::class.java) {
            evaluatePermissionCheck(listOf(" "), { true }) {}
        }
    }
}
