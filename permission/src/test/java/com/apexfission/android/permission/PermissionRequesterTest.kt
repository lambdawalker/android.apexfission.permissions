package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PermissionRequesterTest {
    @Test fun `building a request does not check or launch until onDenied`() {
        val events = mutableListOf<String>()
        val coordinator = PermissionRequestCoordinator(
            missingPermissions = { events += "check"; it },
            launch = { events += "launch" },
        )
        val pending = PendingPermissionRequest { denied ->
            coordinator.start(listOf("camera"), { events += "granted" }, denied)
        }

        assertEquals(emptyList<String>(), events)
        pending onDenied { events += "denied" }
        assertEquals(listOf("check", "launch"), events)
    }

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

    @Test fun `only missing unique permissions launch in the original order`() {
        val launched = mutableListOf<List<String>>()
        val coordinator = PermissionRequestCoordinator(
            missingPermissions = { it.filterNot { name -> name == "camera" } },
            launch = { launched += it },
        )

        coordinator.start(listOf("camera", "audio", "audio", "gps"), {}, {})
        assertEquals(listOf(listOf("audio", "gps")), launched)
    }

    @Test fun `partial grant reports remaining permissions without running protected work`() {
        val granted = mutableSetOf<String>()
        var protectedActions = 0
        var denied: List<String>? = null
        val coordinator = PermissionRequestCoordinator(
            missingPermissions = { it.filterNot(granted::contains) },
            launch = {},
        )

        coordinator.start(listOf("camera", "audio", "gps"), { protectedActions++ }, { denied = it })
        granted += "camera"
        granted += "gps"
        coordinator.onResult()

        assertEquals(0, protectedActions)
        assertEquals(listOf("audio"), denied)
    }

    @Test fun `launcher failure clears pending so a later user action can retry`() {
        var attempts = 0
        val coordinator = PermissionRequestCoordinator(
            missingPermissions = { it },
            launch = { if (++attempts == 1) throw IllegalStateException("launcher unavailable") },
        )

        assertThrows(IllegalStateException::class.java) {
            coordinator.start(listOf("camera"), {}, {})
        }
        var denied = 0
        coordinator.start(listOf("camera"), {}, { denied++ })
        coordinator.onResult()
        assertEquals(2, attempts)
        assertEquals(1, denied)
    }

    @Test fun `completion releases the pending slot before invoking user callbacks`() {
        var launches = 0
        lateinit var coordinator: PermissionRequestCoordinator
        coordinator = PermissionRequestCoordinator(
            missingPermissions = { it },
            launch = { launches++ },
        )
        coordinator.start(listOf("camera"), {}, {
            coordinator.start(listOf("audio"), {}, {})
        })

        coordinator.onResult()
        assertEquals(2, launches)
    }

    @Test fun `empty or blank permission requests never launch`() {
        var launches = 0
        val coordinator = PermissionRequestCoordinator({ it }, { launches++ })
        assertThrows(IllegalArgumentException::class.java) {
            coordinator.start(emptyList(), {}, {})
        }
        assertThrows(IllegalArgumentException::class.java) {
            coordinator.start(listOf("camera", " "), {}, {})
        }
        assertEquals(0, launches)
    }

}
