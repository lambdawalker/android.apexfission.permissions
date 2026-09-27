package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionBundleDecisionTest {
    @Test fun `icon strip centers the overview and each permission`() {
        assertEquals(224, iconScrollOffsetDp(8, -1))
        assertEquals(0, iconScrollOffsetDp(8, 0))
        assertEquals(448, iconScrollOffsetDp(8, 7))
    }

    @Test fun `request batch when at least one outstanding permission can be requested`() {
        assertEquals(BundleAction.Request, bundleAction(listOf(PermissionStatus.Granted, PermissionStatus.NotRequested)))
        assertEquals(BundleAction.Request, bundleAction(listOf(PermissionStatus.PermanentlyDenied, PermissionStatus.RationaleRequired)))
    }

    @Test fun `offer settings only when every outstanding permission lacks a rationale`() {
        assertEquals(BundleAction.Settings, bundleAction(listOf(PermissionStatus.Granted, PermissionStatus.PermanentlyDenied)))
    }

    @Test fun `finish when all permissions are granted`() {
        assertEquals(BundleAction.Complete, bundleAction(listOf(PermissionStatus.Granted, PermissionStatus.Granted)))
    }
}
