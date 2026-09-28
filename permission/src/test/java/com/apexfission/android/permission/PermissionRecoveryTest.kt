package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionRecoveryTest {
    @Test fun `recovery identifies only inferred blocked permissions`() {
        assertEquals(
            listOf("camera", "location"),
            inferredRecoveryPermissions(
                listOf("camera", "microphone", "location", "notifications"),
                listOf(
                    PermissionStatus.PermanentlyDenied,
                    PermissionStatus.RationaleRequired,
                    PermissionStatus.PermanentlyDenied,
                    PermissionStatus.Granted,
                ),
            ),
        )
    }

    @Test fun `no recovery note before request or when rationale is available`() {
        assertEquals(
            emptyList<String>(),
            inferredRecoveryPermissions(
                listOf("camera", "microphone"),
                listOf(PermissionStatus.NotRequested, PermissionStatus.RationaleRequired),
            ),
        )
    }
}
