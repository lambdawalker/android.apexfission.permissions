package com.apexfission.android.permission.requester

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apexfission.android.permission.ui.HandlePermissionBundle
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionDisplayMode
import com.apexfission.android.permission.ui.PermissionOverview
import com.apexfission.android.permission.ui.PermissionOverviewMode

/**
 * Protects [content] until every required Android runtime permission is granted. One button
 * requests required and optional permissions together; the carousel explains each grant.
 *
 * The host must declare every permission in its manifest. This gate intentionally does not handle
 * special app access (such as exact alarms) or install-time permissions, which Android requests
 * through different mechanisms.
 *
 * @param permissions Nonempty list of unique permission descriptions with full pages.
 * @param onBack Back navigation from the explanation UI.
 * @param onNotNow Exit or defer the feature without requesting access.
 * @param displayMode Whether the carousel shows all permissions or only those missing.
 * @param overviewMode Whether the built-in carousel includes a feature overview page. Automatic
 * shows it when more than one permission is visible; Show and Hide override either count.
 * @param autoAdvance Whether to advance through the carousel using each page's configured delay
 * until the first user interaction. Disabled by default.
 * @param recoveryContent Replaces the built-in note shown when local history and Android's
 * rationale signal suggest that a permission prompt may no longer appear. Receives the inferred
 * permission strings; this status is not certain.
 * @param settingsActionLabel Optional caption for the Settings button.
 * @param onOpenSettings Optional host action for the Settings button; defaults to app details.
 * @param content Composable displayed when all required permissions are granted. Its
 * [PermissionGrants] snapshot reports optional grants, including after a partial result.
 */
@Composable
fun HandlePermissions(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = PermissionOverview(),
    displayMode: PermissionDisplayMode = PermissionDisplayMode.All,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    settingsActionLabel: String? = null,
    onOpenSettings: (() -> Unit)? = null,
    content: @Composable (PermissionGrants) -> Unit,
) {
    HandlePermissionBundle(
        permissions = permissions,
        onBack = onBack,
        onNotNow = onNotNow,
        modifier = modifier,
        overview = overview,
        displayMode = displayMode,
        overviewMode = overviewMode,
        autoAdvance = autoAdvance,
        recoveryContent = recoveryContent,
        settingsActionLabel = settingsActionLabel,
        onOpenSettings = onOpenSettings,
        content = content,
    )
}
