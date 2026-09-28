package com.apexfission.android.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.apexfission.android.permission.LocationAccuracy
import com.apexfission.android.permission.PermissionRecipeStep
import com.apexfission.android.permission.PermissionRecipes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies recipe transitions against real device grant state, rather than mocked SDK values. */
@RunWith(AndroidJUnit4::class)
class PlatformRecipeDeviceTest {
    @Test fun foregroundGrantThenBackgroundStepIsSeparate() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(LocationAccuracy.Approximate, PermissionRecipes.grantedLocationAccuracy(context))
        assertEquals(PermissionRecipeStep.Ready,
            PermissionRecipes.foregroundLocation(context, LocationAccuracy.Approximate))
        assertEquals(
            PermissionRecipeStep.RequestRuntime(listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)),
            PermissionRecipes.foregroundLocation(context, LocationAccuracy.Precise),
        )
        val background = PermissionRecipes.backgroundLocation(context, LocationAccuracy.Approximate)
        if (Build.VERSION.SDK_INT == 29) {
            assertEquals(PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)),
                background)
        } else {
            assertEquals(PermissionRecipeStep.OpenAppSettings, background)
        }
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION)
        assertEquals(LocationAccuracy.Precise, PermissionRecipes.grantedLocationAccuracy(context))
        assertEquals(PermissionRecipeStep.Ready,
            PermissionRecipes.foregroundLocation(context, LocationAccuracy.Precise))
    }

    @Test fun notificationRecipeUsesRuntimeStepOnlyOnAndroid13Plus() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val step = PermissionRecipes.notifications(context)
        val expected = when {
            Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ->
                PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.POST_NOTIFICATIONS))
            NotificationManagerCompat.from(context).areNotificationsEnabled() -> PermissionRecipeStep.Ready
            else -> PermissionRecipeStep.OpenAppSettings
        }
        assertEquals(expected, step)
    }
}
