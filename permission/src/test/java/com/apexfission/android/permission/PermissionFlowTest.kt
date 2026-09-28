package com.apexfission.android.permission

import com.apexfission.android.permission.recipe.PermissionPrimaryAction
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.recipe.allPermissionsGranted
import com.apexfission.android.permission.recipe.nextOutstandingPermission
import com.apexfission.android.permission.recipe.primaryAction
import com.apexfission.android.permission.recipe.resolvePermissionStatus
import com.apexfission.android.permission.requester.PermissionGrants
import com.apexfission.android.permission.ui.PermissionDescription
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

    @Test fun `optional denial does not block required access`() {
        val permissions = listOf(
            PermissionDescription("android.permission.CAMERA") {},
            PermissionDescription("android.permission.RECORD_AUDIO", required = false) {},
        )
        val statuses = listOf(PermissionStatus.Granted, PermissionStatus.PermanentlyDenied)
        val grants = PermissionGrants(permissions, statuses)
        assertTrue(grants.canProceed)
        assertTrue(grants.isGranted("android.permission.CAMERA"))
        assertFalse(grants.isGranted("android.permission.RECORD_AUDIO"))
        assertEquals(setOf("android.permission.RECORD_AUDIO"), grants.missingOptional)
        assertEquals(emptySet<String>(), grants.missingRequired)
        assertEquals(
            PermissionStatus.PermanentlyDenied,
            grants.statusByPermission["android.permission.RECORD_AUDIO"])
    }

    @Test fun `missing required permission still blocks access`() {
        val permissions = listOf(
            PermissionDescription("android.permission.CAMERA") {},
            PermissionDescription("android.permission.RECORD_AUDIO", required = false) {},
        )
        val grants = PermissionGrants(
            permissions,
            listOf(PermissionStatus.NotRequested, PermissionStatus.Granted)
        )
        assertFalse(grants.canProceed)
        assertEquals(setOf("android.permission.CAMERA"), grants.missingRequired)
    }

    @Test fun `descriptions remain required by default`() {
        val permissions = listOf(PermissionDescription("android.permission.CAMERA") {})
        assertFalse(PermissionGrants(permissions, listOf(PermissionStatus.NotRequested)).canProceed)
    }

    @Test fun `all optional permissions allow content without a grant`() {
        val permissions = listOf(PermissionDescription("android.permission.RECORD_AUDIO", required = false) {})
        assertTrue(PermissionGrants(permissions, listOf(PermissionStatus.NotRequested)).canProceed)
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
