package com.apexfission.android.permissions.demo

import androidx.compose.foundation.layout.fillMaxWidth
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.apexfission.android.ui.common.ActionCard
import com.apexfission.android.ui.common.CatalogScreen
import com.apexfission.android.permission.recipe.openPermissionRecipeSettings
import com.apexfission.android.permissions.R
import com.apexfission.android.permissions.ui.theme.AndroidpermissionsTheme

@Composable
fun DemoHomeScreen(
    onCarousel: () -> Unit,
    onRecipes: () -> Unit,
    onCallbacks: () -> Unit,
    onArtwork: () -> Unit,
    onSingle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    CatalogScreen(
        title = stringResource(R.string.demo_catalog_title),
        description = stringResource(R.string.demo_catalog_welcome),
        modifier = modifier,
        headerActions = {
            androidx.compose.material3.OutlinedButton(
                onClick = { context.openPermissionRecipeSettings() },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.material3.MaterialTheme.shapes.small,
            ) { androidx.compose.material3.Text(stringResource(R.string.catalog_app_settings)) }
        },
    ) {
        item(key = "quickstart") {
            ActionCard(
                title = stringResource(R.string.demo_quickstart_title),
                description = stringResource(R.string.demo_quickstart_description),
                actionLabel = stringResource(R.string.demo_quickstart_action),
                onClick = { context.startActivity(Intent(context, CameraQuickstartActivity::class.java)) },
                icon = Icons.Default.PhotoCamera,
            )
        }
        item(key = "carousel") {
            ActionCard(
                title = stringResource(R.string.demo_carousel_title),
                description = stringResource(R.string.demo_carousel_description),
                actionLabel = stringResource(R.string.demo_carousel_action),
                onClick = onCarousel,
                icon = Icons.Default.PhotoCamera,
            )
        }
        item(key = "single") {
            ActionCard(
                title = stringResource(R.string.demo_single_title),
                description = stringResource(R.string.demo_single_description),
                actionLabel = stringResource(R.string.demo_single_action),
                onClick = onSingle,
                icon = Icons.Default.PhotoCamera,
            )
        }
        item(key = "recipes") {
            ActionCard(
                title = stringResource(R.string.demo_recipes_title),
                description = stringResource(R.string.demo_recipes_description),
                actionLabel = stringResource(R.string.demo_recipes_action),
                onClick = onRecipes,
                icon = Icons.Default.Tune,
            )
        }
        item(key = "callbacks") {
            ActionCard(
                title = stringResource(R.string.demo_callbacks_title),
                description = stringResource(R.string.demo_callbacks_description),
                actionLabel = stringResource(R.string.demo_callbacks_action),
                onClick = onCallbacks,
                icon = Icons.Default.Lock,
            )
        }
        item(key = "artwork") {
            ActionCard(
                title = stringResource(R.string.demo_artwork_title),
                description = stringResource(R.string.demo_artwork_description),
                actionLabel = stringResource(R.string.demo_artwork_action),
                onClick = onArtwork,
                icon = Icons.Default.Image,
            )
        }
    }
}

@Preview(name = "Demo catalog - light", showBackground = true, widthDp = 393, heightDp = 852)
@Preview(name = "Demo catalog - dark", showBackground = true, widthDp = 393, heightDp = 852,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DemoHomePreview() {
    AndroidpermissionsTheme(dynamicColor = false) { DemoHomeScreen({}, {}, {}, {}, {}) }
}
