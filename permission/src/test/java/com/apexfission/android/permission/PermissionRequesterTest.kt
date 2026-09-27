package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PermissionRequesterTest {
    @Test fun `the infix terminal call attaches denial before launch`() {
        val events = mutableListOf<String>()
        lateinit var coordinator: PermissionRequestCoordinator
        coordinator = PermissionRequestCoordinator(
            missingPermissions = { listOf("camera") },
            launch = { events += "launch"; coordinator.onResult() },
        )
        val request = PendingPermissionRequest { denied ->
            coordinator.start(listOf("camera"), { events += "granted" }, denied)
        }

        request onDenied { events += "denied: ${it.single()}" }
        assertEquals(listOf("launch", "denied: camera"), events)
    }

    @Test fun `grant after prompt resumes protected action once`() {
        val events = mutableListOf<String>()
        var granted = false
        val coordinator = PermissionRequestCoordinator(
            missingPermissions = { if (granted) emptyList() else listOf("camera", "audio") },
            launch = { events += "requested: ${it.joinToString()}" },
        )
        PendingPermissionRequest { denied ->
            coordinator.start(listOf("camera", "audio"), { events += "action" }, denied)
        } onDenied { events += "denied" }

        granted = true
        coordinator.onResult()
        coordinator.onResult() // a duplicate callback cannot run protected work again
        assertEquals(listOf("requested: camera, audio", "action"), events)
    }

    @Test fun `already granted runs immediately without launching`() {
        var actions = 0
        val coordinator = PermissionRequestCoordinator({ emptyList() }, { error("unexpected launch") })
        PendingPermissionRequest { denied ->
            coordinator.start(listOf("camera"), { actions++ }, denied)
        } onDenied { error("unexpected denial") }
        assertEquals(1, actions)
    }

    @Test fun `one pending request at a time and one terminal call`() {
        val coordinator = PermissionRequestCoordinator({ it }, {})
        val first = PendingPermissionRequest { denied -> coordinator.start(listOf("camera"), {}, denied) }
        first onDenied {}
        assertThrows(IllegalStateException::class.java) { first onDenied {} }
        assertThrows(IllegalStateException::class.java) {
            PendingPermissionRequest { denied -> coordinator.start(listOf("audio"), {}, denied) } onDenied {}
        }
    }
}
