package com.apexfission.android.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionBundleDecisionTest {
    @Test fun `automatic overview follows visible permission count`() {
        assertEquals(false, shouldShowOverview(1, PermissionOverviewMode.Automatic))
        assertEquals(true, shouldShowOverview(2, PermissionOverviewMode.Automatic))
    }

    @Test fun `overview can be shown for one permission or hidden for several`() {
        assertEquals(true, shouldShowOverview(1, PermissionOverviewMode.Show))
        assertEquals(false, shouldShowOverview(2, PermissionOverviewMode.Hide))
        assertEquals(false, shouldShowOverview(1, PermissionOverviewMode.Hide))
    }

    @Test fun `all mode keeps granted pages in their original order`() {
        assertEquals(
            listOf(0, 1, 2),
            visiblePermissionIndices(
                listOf(PermissionStatus.Granted, PermissionStatus.NotRequested, PermissionStatus.Granted),
                PermissionDisplayMode.All,
            ),
        )
    }

    @Test fun `missing mode removes granted pages and preserves remaining order`() {
        assertEquals(
            listOf(1, 3),
            visiblePermissionIndices(
                listOf(
                    PermissionStatus.Granted,
                    PermissionStatus.RationaleRequired,
                    PermissionStatus.Granted,
                    PermissionStatus.PermanentlyDenied,
                ),
                PermissionDisplayMode.MissingOnly,
            ),
        )
        assertEquals(
            emptyList<Int>(),
            visiblePermissionIndices(listOf(PermissionStatus.Granted), PermissionDisplayMode.MissingOnly),
        )
    }

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
