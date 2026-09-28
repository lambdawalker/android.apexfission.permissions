package com.apexfission.android.permissions.demo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexfission.android.permission.ui.DefaultPermissionPage

/** Shows all four supported artwork types; the icon strip is configured separately on a permission. */
@Composable
fun HeroArtworkDemo(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val bitmap = remember {
        Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            canvas.drawColor(Color.rgb(18, 105, 85))
            canvas.drawCircle(48f, 48f, 30f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(217, 248, 238)
            })
        }
    }
    DisposableEffect(bitmap) { onDispose { bitmap.recycle() } }
    val drawable = remember {
        GradientDrawable(GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.rgb(18, 105, 85), Color.rgb(112, 222, 194)))
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(onClick = onBack) { Text("Back to demos") }
        Text("Hero artwork gallery", style = MaterialTheme.typography.headlineMedium)
        Text("Each card uses DefaultPermissionPage. PermissionDescription.icon still controls the carousel's small icon.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        ArtworkCard { DefaultPermissionPage("Camera", heroImage = Icons.Default.PhotoCamera,
            title = "Vector hero", body = "Tinted Material icon in the default hero frame.") }
        ArtworkCard { DefaultPermissionPage("Bitmap", heroImage = bitmap,
            title = "Bitmap hero", body = "Host-owned Android Bitmap scaled inside the frame.") }
        ArtworkCard { DefaultPermissionPage("Drawable resource", heroImage = android.R.drawable.ic_menu_camera,
            title = "Drawable resource hero", body = "A drawable ID loaded from Android resources.") }
        ArtworkCard { DefaultPermissionPage("Drawable object", heroImage = drawable,
            title = "Drawable object hero", body = "A host-supplied Android Drawable instance.") }
    }
}

@Composable
private fun ArtworkCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) { content() }
    }
}
