package com.apexfission.android.permission.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apexfission.android.permission.requester.DefaultPermissionRecovery
import com.apexfission.android.permission.recipe.PermissionPrimaryAction
import com.apexfission.android.permission.recipe.PermissionStatus
import com.apexfission.android.permission.R
import com.apexfission.android.permission.recipe.nextOutstandingPermission
import com.apexfission.android.permission.recipe.primaryAction
import kotlinx.coroutines.launch

/**
 * Metadata and required page for one runtime permission. The [permission] value must be
 * declared by the app. The host supplies the entire [page]; the surrounding permission UI
 * owns navigation, the icon selector, status messages, and request actions.
 *
 * @property permission Android runtime permission string, e.g. `android.permission.CAMERA`.
 * @property label Spoken name in the icon selector, e.g. `Camera`. Defaults to the name in
 * [PermissionVisualDefaults], or a readable permission suffix for unknown strings. Override it
 * with feature-specific or localized wording when appropriate.
 * @property icon Small icon displayed in the selection list. Defaults to the
 * matching icon in [PermissionVisualDefaults], or a lock for unknown strings.
 * @property autoAdvanceDelayMillis Time spent on this page before optional carousel autoplay
 * advances. The host can calculate it with [estimateReadingDelayMillis] from its visible copy.
 * @property required Whether this grant is needed before protected content can appear. Optional
 * permissions are still included in a batch request while the explanation screen is shown.
 * @property page Full page composable for both batch and individual screens. The host may use
 * [DefaultPermissionPage] for a ready-made hero and explanation. The icon strip stays outside it.
 */
class PermissionDescription(
    val permission: String,
    val label: String = PermissionVisualDefaults.forPermission(permission).label,
    val icon: ImageVector = PermissionVisualDefaults.forPermission(permission).icon,
    val autoAdvanceDelayMillis: Long = 6_000L,
    val required: Boolean = true,
    val page: @Composable () -> Unit,
) {
    init { require(autoAdvanceDelayMillis > 0) { "autoAdvanceDelayMillis must be positive" } }
}

/**
 * A generic permission primer. For multiple permissions, icons wrap into rows and select a page
 * of the horizontal carousel. The current page determines the action button. This composable
 * only renders UI and delegates request/settings actions to the host.
 *
 * @param permissions Nonempty list with unique Android permission strings.
 * @param statuses Current statuses in the same order as [permissions].
 * @param onRequest Request the permission at the given index after the user taps the button.
 * @param onOpenSettings Open app settings for the permission at the given index.
 * @param onComplete Called if all permissions are already granted in a standalone screen.
 * @param initialPage Page shown when this screen first enters composition.
 * @param recoveryContent Content for a status inferred to need Settings; receives its permission string.
 * @param settingsActionLabel Optional caption for the Settings button.
 * @param requestActionLabel Optional caption for the request button; defaults to a generic
 * label based on the selected permission. This belongs to the screen, not to a page.
 */
@Composable
fun PermissionScreen(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    onRequest: (Int) -> Unit,
    onOpenSettings: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {},
    initialPage: Int = 0,
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    settingsActionLabel: String? = null,
    requestActionLabel: String? = null,
) {
    require(permissions.isNotEmpty()) { "At least one permission is required" }
    require(permissions.size == statuses.size) { "Each permission needs one status" }
    require(permissions.map { it.permission }.distinct().size == permissions.size) {
        "Permissions must be unique"
    }
    require(initialPage in permissions.indices) { "initialPage is out of range" }
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { permissions.size })
    val scope = rememberCoroutineScope()
    val current = pager.currentPage.coerceIn(permissions.indices)
    val permission = permissions[current]
    val status = statuses[current]
    val next = nextOutstandingPermission(statuses, current)
    val action = primaryAction(status)

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.permission_back))
                }
                Text(
                    text = stringResource(R.string.permission_progress, current + 1, permissions.size),
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.size(44.dp))
            }
            if (permissions.size > 1) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    permissions.forEachIndexed { index, item ->
                        val selected = index == current
                        val size by animateDpAsState(if (selected) 56.dp else 44.dp, label = "permission icon size")
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(56.dp)
                                .semantics { contentDescription = item.label }
                                .clickable { scope.launch { pager.animateScrollToPage(index) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier.size(size).background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape,
                                ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (statuses[index] == PermissionStatus.Granted) Icons.Default.Check else item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (selected) 27.dp else 22.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            HorizontalPager(
                state = pager,
                key = { permissions[it].permission },
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                val item = permissions[page]
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item.page()
                    if (!item.required) {
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.permission_optional),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (status == PermissionStatus.PermanentlyDenied) {
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    recoveryContent(listOf(permission.permission))
                }
            }
            Button(
                onClick = {
                    when (action) {
                        PermissionPrimaryAction.Request -> onRequest(current)
                        PermissionPrimaryAction.Settings -> onOpenSettings(current)
                        PermissionPrimaryAction.Next -> if (next == null) onComplete()
                            else scope.launch { pager.animateScrollToPage(next) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    when (action) {
                        PermissionPrimaryAction.Request -> requestActionLabel
                            ?: stringResource(R.string.permission_generic_allow, permission.label)
                        PermissionPrimaryAction.Settings -> settingsActionLabel ?: stringResource(R.string.permission_open_settings)
                        PermissionPrimaryAction.Next -> if (next == null) stringResource(R.string.permission_done)
                            else stringResource(R.string.permission_next)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(onClick = onNotNow, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.camera_permission_not_now))
            }
        }
    }
}
