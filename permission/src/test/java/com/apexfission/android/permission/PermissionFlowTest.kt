package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionFlowTest {
    @Test fun `status keeps rationale above request history`() {
        assertEquals(PermissionStatus.RationaleRequired, resolvePermissionStatus(false, true, true))
        assertEquals(PermissionStatus.PermanentlyDenied, resolvePermissionStatus(false, false, true))
        assertEquals(PermissionStatus.NotRequested, resolvePermissionStatus(false, false, false))
        assertEquals(PermissionStatus.Granted, resolvePermissionStatus(true, false, true))
    }

    @Test fun `only all granted unlocks protected content`() {
        assertTrue(allPermissionsGranted(listOf(PermissionStatus.Granted, PermissionStatus.Granted)))
        assertFalse(allPermissionsGranted(listOf(PermissionStatus.Granted, PermissionStatus.NotRequested)))
        assertFalse(allPermissionsGranted(emptyList()))
    }

    @Test fun `next outstanding permission wraps and skips granted pages`() {
        val statuses = listOf(PermissionStatus.NotRequested, PermissionStatus.Granted, PermissionStatus.RationaleRequired)
        assertEquals(2, nextOutstandingPermission(statuses, 0))
        assertEquals(0, nextOutstandingPermission(statuses, 2))
        assertEquals(null, nextOutstandingPermission(listOf(PermissionStatus.Granted), 0))
    }

    @Test fun `the primary action depends on the currently displayed status`() {
        assertEquals(PermissionPrimaryAction.Request, primaryAction(PermissionStatus.NotRequested))
        assertEquals(PermissionPrimaryAction.Request, primaryAction(PermissionStatus.RationaleRequired))
        assertEquals(PermissionPrimaryAction.Settings, primaryAction(PermissionStatus.PermanentlyDenied))
        assertEquals(PermissionPrimaryAction.Next, primaryAction(PermissionStatus.Granted))
    }
}
