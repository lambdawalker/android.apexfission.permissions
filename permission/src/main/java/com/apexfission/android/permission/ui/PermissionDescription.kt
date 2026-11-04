package com.apexfission.android.permission.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private const val DefaultPermissionLabel = "\u0000permission-default-label"

/**
 * Metadata and required page for one runtime permission. The [permission] value must be
 * declared by the app. The host supplies the entire [page]; the surrounding permission UI
 * owns navigation, the icon selector, status messages, and request actions.
 *
 * @property permission Android runtime permission string, e.g. `android.permission.CAMERA`.
 * @property label Spoken name in the icon selector, e.g. `Camera`. The property retains the name in
 * [PermissionVisualDefaults], or a readable permission suffix for unknown strings. When omitted,
 * the library localizes known names in the icon selector using the current device locale. Override it
 * with feature-specific or localized wording when appropriate.
 * @property icon Small icon displayed in the selection list. Defaults to the
 * matching icon in [PermissionVisualDefaults], or a lock for unknown strings.
 * @property autoAdvanceDelayMillis Time spent on this page before optional carousel autoplay
 * advances. The host can calculate it with [estimateReadingDelayMillis] from its visible copy.
 * @property required Whether this grant is needed before protected content can appear. Optional
 * permissions are still included in a batch request while the explanation screen is shown.
 * @property page Full page composable for the batch carousel. The host may use
 * [DefaultPermissionPage] for a ready-made hero and explanation. The icon strip stays outside it.
 */
class PermissionDescription(
    val permission: String,
    label: String = DefaultPermissionLabel,
    val icon: ImageVector = PermissionVisualDefaults.forPermission(permission).icon,
    val autoAdvanceDelayMillis: Long = 6_000L,
    val required: Boolean = true,
    val page: @Composable () -> Unit,
) {
    /** Stable metadata; an explicit label is displayed verbatim, including an empty string. */
    val label: String = if (label == DefaultPermissionLabel) {
        PermissionVisualDefaults.forPermission(permission).label
    } else label

    @get:StringRes
    internal val defaultLabelResource: Int? = if (label == DefaultPermissionLabel) {
        PermissionVisualDefaults.labelResource(permission)
    } else null

    init { require(autoAdvanceDelayMillis > 0) { "autoAdvanceDelayMillis must be positive" } }
}
