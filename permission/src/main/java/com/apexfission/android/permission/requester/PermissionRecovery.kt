package com.apexfission.android.permission.requester

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apexfission.android.permission.R

/**
 * Default note for permissions inferred to need recovery. The inference can be wrong, so the
 * note does not claim that Android has permanently blocked a request. Replace this composable
 * with feature-specific guidance via `recoveryContent` in a permission screen or gate.
 */
@Composable
fun DefaultPermissionRecovery(permissions: List<String>) {
    if (permissions.isEmpty()) return
    Text(
        stringResource(R.string.permission_recovery_uncertain),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
