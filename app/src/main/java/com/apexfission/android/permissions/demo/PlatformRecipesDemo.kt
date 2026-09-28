package com.apexfission.android.permissions.demo

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.apexfission.android.permission.LocationAccuracy
import com.apexfission.android.permission.PermissionRecipeStep
import com.apexfission.android.permission.PermissionRecipes
import com.apexfission.android.permission.VisualMediaSelection
import com.apexfission.android.permission.openPermissionRecipeSettings
import com.apexfission.android.permission.rememberVisualMediaPicker

private enum class Recipe { Approximate, Precise, Background, Notifications }

/** Runnable host example: every recipe is re-evaluated after a result or return from Settings. */
@Composable
fun PlatformRecipesDemo(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var refresh by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<Recipe?>(null) }
    var selectedUri by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        refresh++
        message = if (result.values.all { it }) "Request finished. Check the access shown below."
            else "Some access was declined. You can continue without it or try again later."
    }
    val photoPicker = rememberVisualMediaPicker { uri ->
        selectedUri = uri?.toString()
        message = if (uri == null) "Photo selection canceled." else "Selected a photo or video."
    }

    // Reading refresh ensures the cards update on return from Settings and after a runtime result.
    val steps = remember(context, refresh) {
        Recipe.entries.associateWith { recipe ->
            when (recipe) {
                Recipe.Approximate -> PermissionRecipes.foregroundLocation(context, LocationAccuracy.Approximate)
                Recipe.Precise -> PermissionRecipes.foregroundLocation(context, LocationAccuracy.Precise)
                Recipe.Background -> PermissionRecipes.backgroundLocation(context, LocationAccuracy.Approximate)
                Recipe.Notifications -> PermissionRecipes.notifications(context)
            }
        }
    }
    val accuracy = remember(context, refresh) { PermissionRecipes.grantedLocationAccuracy(context) }

    fun start(recipe: Recipe) {
        val step = steps.getValue(recipe)
        when (step) {
            PermissionRecipeStep.Ready -> message = "${recipe.label()} access is available."
            PermissionRecipeStep.SystemControlledPrompt ->
                message = "Android controls when this notification prompt appears for this target SDK."
            PermissionRecipeStep.OpenAppSettings -> confirmation = recipe
            is PermissionRecipeStep.RequestRuntime -> {
                if (Manifest.permission.ACCESS_BACKGROUND_LOCATION in step.permissions) {
                    confirmation = recipe // Background access needs its own explanation and action.
                } else {
                    message = null
                    runtimeLauncher.launch(step.permissions.toTypedArray())
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(onClick = onBack) { Text("Back to demos") }
        Text("Platform recipes", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Try each request when you need that feature. These cards show the next action returned by the library.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (message != null) Text(message!!, color = MaterialTheme.colorScheme.primary)
        Text("Foreground accuracy: ${accuracy?.name ?: "none"}", style = MaterialTheme.typography.bodyMedium)
        RecipeCard("Approximate location", "Ask for coarse location only.",
            steps.getValue(Recipe.Approximate), onClick = { start(Recipe.Approximate) })
        RecipeCard("Precise location", "Ask for coarse and fine together. The user may still choose approximate.",
            steps.getValue(Recipe.Precise), onClick = { start(Recipe.Precise) })
        RecipeCard("Background location", "First grant foreground location, then request background access separately.",
            steps.getValue(Recipe.Background), onClick = { start(Recipe.Background) })
        RecipeCard("Notifications", "Ask when you want to send updates; Settings can restore disabled notifications.",
            steps.getValue(Recipe.Notifications), onClick = { start(Recipe.Notifications) })
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Photo picker", style = MaterialTheme.typography.titleMedium)
                Text("Choose a photo or video without a storage permission.")
                Button(onClick = { photoPicker.launch(VisualMediaSelection.ImageOrVideo) }) {
                    Text("Choose media")
                }
                if (selectedUri != null) Text("Selected: $selectedUri")
            }
        }
        if (confirmation != null) {
            val recipe = confirmation!!
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Continue with ${recipe.label().lowercase()}?", style = MaterialTheme.typography.titleMedium)
                    Text(if (recipe == Recipe.Background) {
                        "A feature may need location even while the app is closed. Grant foreground access first. On Android 11 or later, choose ${backgroundOptionLabel(context)} in app Settings. You can decline and keep using the demo."
                    } else {
                        "Open this app's Settings to adjust ${recipe.label().lowercase()} access. You can decline and keep using the demo."
                    })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { confirmation = null }) { Text("Not now") }
                        Button(onClick = {
                            confirmation = null
                            // Recheck at the gesture: the permission could have changed while this card was shown.
                            when (val next = when (recipe) {
                                Recipe.Background -> PermissionRecipes.backgroundLocation(context, LocationAccuracy.Approximate)
                                Recipe.Notifications -> PermissionRecipes.notifications(context)
                                Recipe.Approximate -> PermissionRecipes.foregroundLocation(context, LocationAccuracy.Approximate)
                                Recipe.Precise -> PermissionRecipes.foregroundLocation(context, LocationAccuracy.Precise)
                            }) {
                                PermissionRecipeStep.OpenAppSettings -> context.openPermissionRecipeSettings()
                                is PermissionRecipeStep.RequestRuntime -> runtimeLauncher.launch(next.permissions.toTypedArray())
                                PermissionRecipeStep.Ready -> { refresh++; message = "Access is already available." }
                                PermissionRecipeStep.SystemControlledPrompt -> message = "Android controls this prompt."
                            }
                        }) { Text("Continue") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(title: String, description: String, step: PermissionRecipeStep, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(step.label(), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Button(onClick = onClick) { Text(if (step == PermissionRecipeStep.Ready) "Check access" else "Try recipe") }
        }
    }
}

private fun Recipe.label(): String = when (this) {
    Recipe.Approximate -> "Approximate location"
    Recipe.Precise -> "Precise location"
    Recipe.Background -> "Background location"
    Recipe.Notifications -> "Notifications"
}

private fun PermissionRecipeStep.label(): String = when (this) {
    PermissionRecipeStep.Ready -> "Ready"
    is PermissionRecipeStep.RequestRuntime -> "Request: ${permissions.joinToString { it.substringAfterLast('.') }}"
    PermissionRecipeStep.OpenAppSettings -> "Continue in app Settings"
    PermissionRecipeStep.SystemControlledPrompt -> "Android controls this prompt"
}

private fun backgroundOptionLabel(context: Context): String = if (Build.VERSION.SDK_INT >= 30) {
    context.packageManager.backgroundPermissionOptionLabel.toString()
} else "Allow all the time"
