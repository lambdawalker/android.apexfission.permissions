package com.apexfission.android.permissions.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apexfission.android.permissions.ui.theme.AndroidpermissionsTheme

@Composable
fun DemoHomeScreen(
    onCarousel: () -> Unit,
    onRecipes: () -> Unit,
    onCallbacks: () -> Unit,
    onArtwork: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Permission demos", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Explore the Compose screen, Android permission recipes, and code-only callbacks.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DemoCard("Permission carousel", "Camera access with optional microphone narration.",
            "Open carousel", onCarousel) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
        }
        DemoCard("Platform recipes", "Location, background access, notifications, and photo selection.",
            "Open recipes", onRecipes) {
            Icon(Icons.Default.Tune, contentDescription = null)
        }
        DemoCard("Code-only callbacks", "Compare an Activity-owned launcher with the library requester.",
            "Open callbacks", onCallbacks) {
            Icon(Icons.Default.Lock, contentDescription = null)
        }
        DemoCard("Hero artwork", "Inspect vector, bitmap, drawable resource, and Drawable heroes.",
            "Open artwork gallery", onArtwork) {
            Icon(Icons.Default.Image, contentDescription = null)
        }
    }
}

@Composable
private fun DemoCard(
    title: String, description: String, action: String, onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            icon()
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick) { Text(action) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoHomePreview() {
    AndroidpermissionsTheme { DemoHomeScreen({}, {}, {}, {}) }
}
