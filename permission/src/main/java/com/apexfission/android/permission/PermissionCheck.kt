package com.apexfission.android.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Result of one synchronous runtime permission check. [missing] is a snapshot, not an observable
 * permission state. The protected action has already run if [missing] is empty.
 */
class PermissionCheckResult internal constructor(
    val missing: List<String>,
) {
    /** Runs [onMissing] only when at least one permission was missing at check time. */
    infix fun otherwise(onMissing: (List<String>) -> Unit): PermissionCheckResult = apply {
        if (missing.isNotEmpty()) onMissing(missing)
    }
}

/**
 * Checks manifest-declared Android runtime permissions without presenting library UI. Runs
 * [onGranted] immediately if all are currently granted; otherwise [PermissionCheckResult.otherwise]
 * receives the missing names. The host owns an Activity, Fragment, or Compose result launcher
 * and decides when to request them. After a grant result, call this method again before running
 * protected work; a request is asynchronous and this function does not retain [onGranted].
 *
 * The caller must provide a nonempty set of runtime permissions. Special app access and
 * permissions requiring staged platform requests need separate host-managed flows.
 */
fun Context.runIfPermissionsGranted(
    vararg permissions: String,
    onGranted: () -> Unit,
): PermissionCheckResult = runIfPermissionsGranted(permissions.asList(), onGranted)

/** List variant of [runIfPermissionsGranted] for dynamically assembled permissions. */
fun Context.runIfPermissionsGranted(
    permissions: List<String>,
    onGranted: () -> Unit,
): PermissionCheckResult = evaluatePermissionCheck(permissions, { permission ->
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}, onGranted)

internal fun evaluatePermissionCheck(
    permissions: List<String>,
    isGranted: (String) -> Boolean,
    onGranted: () -> Unit,
): PermissionCheckResult {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.all { it.isNotBlank() }) { "Permissions must not be blank" }
    val missing = permissions.distinct().filterNot(isGranted)
    if (missing.isEmpty()) onGranted()
    return PermissionCheckResult(missing)
}
