package com.apexfission.android.permission.recipe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** The location accuracy needed by the feature, rather than the accuracy requested by the user. */
enum class LocationAccuracy { Approximate, Precise }

/** Next host action for a platform-aware request. Never launch a step during composition. */
sealed interface PermissionRecipeStep {
    /** The requested access is currently available. Recheck before using protected APIs. */
    data object Ready : PermissionRecipeStep

    /** Launch these permissions together with an Activity-owned runtime permission launcher. */
    data class RequestRuntime(val permissions: List<String>) : PermissionRecipeStep

    /** Show an explanation and a decline option, then let the user open this app's settings. */
    data object OpenAppSettings : PermissionRecipeStep

    /** On Android 13 with target SDK below 33, Android decides when to show the dialog. */
    data object SystemControlledPrompt : PermissionRecipeStep
}

/**
 * Small, explicit recipes for permission flows that cannot safely be put in a generic batch.
 * Call again after the result or after returning from Settings. The host owns the launcher,
 * explanation, refusal handling, and manifest declarations.
 */
object PermissionRecipes {
    /** Request coarse alone, or coarse and fine together for a precise-location feature. */
    fun foregroundLocation(context: Context, accuracy: LocationAccuracy): PermissionRecipeStep =
        foregroundLocationStep(accuracy, context.grantedLocationPermissions())

    /**
     * First obtain foreground access. Android 10 requests background separately; Android 11+
     * requires the user to enable it in Settings after a host-provided educational screen.
     * On Android 9 and lower, foreground access covers background use.
     */
    fun backgroundLocation(context: Context, accuracy: LocationAccuracy): PermissionRecipeStep =
        backgroundLocationStep(Build.VERSION.SDK_INT, accuracy, context.grantedLocationPermissions())

    /**
     * Request POST_NOTIFICATIONS on Android 13+ when the host targets 33+. Older targets let
     * Android control dialog timing. If notifications are disabled outside that request path,
     * direct the user to Settings after an explanation.
     */
    fun notifications(context: Context): PermissionRecipeStep = notificationStep(
        sdkInt = Build.VERSION.SDK_INT,
        targetSdk = context.applicationInfo.targetSdkVersion,
        runtimeGranted = context.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS),
        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
    )

    /** Returns the actual foreground accuracy, which may be approximate after a precise request. */
    fun grantedLocationAccuracy(context: Context): LocationAccuracy? =
        grantedLocationAccuracy(context.grantedLocationPermissions())
}

/** Opens app settings for a [PermissionRecipeStep.OpenAppSettings] action. */
fun Context.openPermissionRecipeSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Context.grantedLocationPermissions(): Set<String> = buildSet {
    listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION).forEach { permission ->
        if (isPermissionGranted(permission)) add(permission)
    }
}

internal fun grantedLocationAccuracy(granted: Set<String>): LocationAccuracy? = when {
    Manifest.permission.ACCESS_FINE_LOCATION in granted -> LocationAccuracy.Precise
    Manifest.permission.ACCESS_COARSE_LOCATION in granted -> LocationAccuracy.Approximate
    else -> null
}

internal fun foregroundLocationStep(
    accuracy: LocationAccuracy,
    granted: Set<String>,
): PermissionRecipeStep = when (accuracy) {
    LocationAccuracy.Approximate -> if (grantedLocationAccuracy(granted) != null)
        PermissionRecipeStep.Ready else PermissionRecipeStep.RequestRuntime(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION))
    LocationAccuracy.Precise -> if (grantedLocationAccuracy(granted) == LocationAccuracy.Precise)
        PermissionRecipeStep.Ready else PermissionRecipeStep.RequestRuntime(listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
}

internal fun backgroundLocationStep(
    sdkInt: Int,
    accuracy: LocationAccuracy,
    granted: Set<String>,
): PermissionRecipeStep {
    val foreground = foregroundLocationStep(accuracy, granted)
    if (foreground != PermissionRecipeStep.Ready) return foreground
    if (sdkInt < 29 || Manifest.permission.ACCESS_BACKGROUND_LOCATION in granted) return PermissionRecipeStep.Ready
    return if (sdkInt == 29) PermissionRecipeStep.RequestRuntime(
        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    else PermissionRecipeStep.OpenAppSettings
}

internal fun notificationStep(
    sdkInt: Int,
    targetSdk: Int,
    runtimeGranted: Boolean,
    notificationsEnabled: Boolean,
): PermissionRecipeStep = when {
    sdkInt < 33 -> if (notificationsEnabled) PermissionRecipeStep.Ready else PermissionRecipeStep.OpenAppSettings
    runtimeGranted -> if (notificationsEnabled) PermissionRecipeStep.Ready else PermissionRecipeStep.OpenAppSettings
    targetSdk >= 33 -> PermissionRecipeStep.RequestRuntime(listOf(Manifest.permission.POST_NOTIFICATIONS))
    else -> PermissionRecipeStep.SystemControlledPrompt
}
