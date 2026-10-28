package com.apexfission.android.permissions.demo

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apexfission.android.permission.requester.HandlePermissions
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionOverviewMode
import com.apexfission.android.permission.recipe.openPermissionRecipeSettings

private const val TITLE = "Scan documents"
private const val BODY = "Allow camera access to capture a document. Since there is only one permission and overview is hidden, the icon strip is omitted."

@Composable
fun SinglePermissionDemo(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    HandlePermissions(
        permissions = listOf(
            PermissionDescription(
                permission = Manifest.permission.CAMERA,
                label = "Camera",
                icon = Icons.Default.PhotoCamera,
            ) {
                DefaultPermissionPage(
                    label = "Camera",
                    heroImage = Icons.Default.PhotoCamera,
                    title = TITLE,
                    body = BODY,
                )
            },
        ),
        modifier = modifier,
        onBack = onBack,
        onNotNow = onBack,
        overviewMode = PermissionOverviewMode.Hide,
        settingsActionLabel = "Review access in Settings",
        onOpenSettings = { context.openPermissionRecipeSettings() },
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Camera permission granted!", style = MaterialTheme.typography.titleLarge)
        }
    }
}
