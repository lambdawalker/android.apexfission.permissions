package com.apexfission.android.permissions.demo

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apexfission.android.permission.requester.PermissionRequester
import com.apexfission.android.permission.requester.runIfPermissionsGranted

/** Demonstrates both code-only paths without composing the library's permission screen. */
@Composable
fun CodeOnlyDemo(requester: PermissionRequester, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var result by remember { mutableStateOf("Choose an action to inspect its result.") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // Results can be partial; inspect current grants before protected work.
        context.runIfPermissionsGranted(Manifest.permission.CAMERA) {
            result = "Camera granted. The host can start its feature."
        } otherwise { missing ->
            result = "Still missing: ${missing.joinToString { it.substringAfterLast('.') }}"
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(onClick = onBack) { Text("Back to demos") }
        Text("Code-only requests", style = MaterialTheme.typography.headlineMedium)
        Text("Two ways to handle runtime access with your own UI. Both start only after a tap.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(result, color = MaterialTheme.colorScheme.primary)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Host-owned launcher", style = MaterialTheme.typography.titleMedium)
                Text("Check camera synchronously, then launch only if it is missing. The host handles denial.")
                Button(onClick = {
                    context.runIfPermissionsGranted(Manifest.permission.CAMERA) {
                        result = "Camera already granted. The host can start its feature."
                    } otherwise { missing -> launcher.launch(missing.toTypedArray()) }
                }) { Text("Check with your own launcher") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Library-owned launcher", style = MaterialTheme.typography.titleMedium)
                Text("Request camera and microphone together. Both grants are required for the success callback.")
                Button(onClick = {
                    with(requester) {
                        requestPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
                            result = "Camera and microphone granted. The host can start its feature."
                        } onDenied { missing ->
                            result = "Still missing: ${missing.joinToString { it.substringAfterLast('.') }}"
                        }
                    }
                }) { Text("Use the library requester") }
            }
        }
    }
}
