package com.apexfission.android.permission

/**
 * Current grant snapshot supplied to protected content. Required permissions determine
 * [canProceed]; optional permissions can be missing while the feature remains available.
 * Android can revoke grants later, so use this snapshot only for the current composition.
 */
class PermissionGrants internal constructor(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
) {
    init {
        require(permissions.isNotEmpty() && permissions.size == statuses.size) {
            "Each permission needs one status"
        }
    }

    /** Status for each permission, including both required and optional entries. */
    val statusByPermission: Map<String, PermissionStatus> =
        permissions.mapIndexed { index, description -> description.permission to statuses[index] }.toMap()

    /** Required permissions that have not been granted. */
    val missingRequired: Set<String> = permissions.indices
        .filter { permissions[it].required && statuses[it] != PermissionStatus.Granted }
        .mapTo(linkedSetOf()) { permissions[it].permission }

    /** Optional permissions that have not been granted. */
    val missingOptional: Set<String> = permissions.indices
        .filter { !permissions[it].required && statuses[it] != PermissionStatus.Granted }
        .mapTo(linkedSetOf()) { permissions[it].permission }

    /** Whether protected content may be displayed. */
    val canProceed: Boolean get() = missingRequired.isEmpty()

    /** Checks a permission in this snapshot. Unknown permission names return false. */
    fun isGranted(permission: String): Boolean = statusByPermission[permission] == PermissionStatus.Granted
}
