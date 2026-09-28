package com.apexfission.android.permission

import android.Manifest
import com.apexfission.android.permission.recipe.LocationAccuracy
import com.apexfission.android.permission.recipe.PermissionRecipeStep
import com.apexfission.android.permission.recipe.backgroundLocationStep
import com.apexfission.android.permission.recipe.foregroundLocationStep
import com.apexfission.android.permission.recipe.grantedLocationAccuracy
import com.apexfission.android.permission.recipe.notificationStep
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionRecipesTest {
    @Test fun `approximate foreground asks only for coarse location`() {
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.ACCESS_COARSE_LOCATION)),
            foregroundLocationStep(LocationAccuracy.Approximate, emptySet())
        )
    }

    @Test fun `precise foreground requests coarse and fine together`() {
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)),
            foregroundLocationStep(LocationAccuracy.Precise, emptySet())
        )
        assertEquals(
            LocationAccuracy.Approximate,
            grantedLocationAccuracy(setOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        )
        assertEquals(
            LocationAccuracy.Precise,
            grantedLocationAccuracy(setOf(Manifest.permission.ACCESS_FINE_LOCATION))
        )
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)),
            foregroundLocationStep(
                LocationAccuracy.Precise,
                setOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        )
    }

    @Test fun `background is requested only after foreground on Android 10`() {
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.ACCESS_COARSE_LOCATION)),
            backgroundLocationStep(29, LocationAccuracy.Approximate, emptySet())
        )
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)),
            backgroundLocationStep(
                29, LocationAccuracy.Approximate,
                setOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        )
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)),
            backgroundLocationStep(
                29, LocationAccuracy.Precise,
                setOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        )
    }

    @Test fun `background uses settings on Android 11 and is unnecessary before Android 10`() {
        val foreground = setOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(
            PermissionRecipeStep.OpenAppSettings,
            backgroundLocationStep(30, LocationAccuracy.Approximate, foreground)
        )
        assertEquals(
            PermissionRecipeStep.Ready,
            backgroundLocationStep(28, LocationAccuracy.Approximate, foreground)
        )
        assertEquals(
            PermissionRecipeStep.Ready,
            backgroundLocationStep(
                34, LocationAccuracy.Approximate,
                foreground + Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )
    }

    @Test fun `notification prompt only exists with host controlled timing on Android 13`() {
        assertEquals(PermissionRecipeStep.OpenAppSettings, notificationStep(32, 32, true, false))
        assertEquals(PermissionRecipeStep.Ready, notificationStep(32, 32, true, true))
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.POST_NOTIFICATIONS)),
            notificationStep(33, 33, false, false)
        )
        assertEquals(PermissionRecipeStep.SystemControlledPrompt, notificationStep(33, 32, false, false))
        assertEquals(PermissionRecipeStep.OpenAppSettings, notificationStep(33, 33, true, false))
        assertEquals(PermissionRecipeStep.Ready, notificationStep(33, 33, true, true))
    }
}
