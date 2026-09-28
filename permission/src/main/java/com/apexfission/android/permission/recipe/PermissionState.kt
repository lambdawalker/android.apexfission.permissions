package com.apexfission.android.permission.recipe

/** Observable status of a runtime permission. */
enum class PermissionStatus {
    Granted, NotRequested, RationaleRequired,
    /** Inferred from local request history and Android's rationale signal; not a definitive platform flag. */
    PermanentlyDenied,
}

internal fun resolvePermissionStatus(
    granted: Boolean,
    shouldShowRationale: Boolean,
    requestedBefore: Boolean,
): PermissionStatus = when {
    granted -> PermissionStatus.Granted
    shouldShowRationale -> PermissionStatus.RationaleRequired
    requestedBefore -> PermissionStatus.PermanentlyDenied
    else -> PermissionStatus.NotRequested
}

/** Permissions that may require Settings, preserving the caller's order. */
internal fun inferredRecoveryPermissions(
    permissions: List<String>, statuses: List<PermissionStatus>,
): List<String> {
    require(permissions.size == statuses.size) { "Each permission needs one status" }
    return permissions.zip(statuses).filter { it.second == PermissionStatus.PermanentlyDenied }
        .map { it.first }
}

internal fun allPermissionsGranted(statuses: List<PermissionStatus>): Boolean =
    statuses.isNotEmpty() && statuses.all { it == PermissionStatus.Granted }
