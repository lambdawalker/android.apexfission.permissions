package com.apexfission.android.permission

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Activity-owned, code-only runtime permission requester. Construct it as an Activity property,
 * before the Activity reaches STARTED, so its result launcher is registered with the lifecycle.
 * No library explanation screen is displayed. The host must declare the permissions in its
 * manifest and must call [requestPermissions] in response to a user action.
 *
 * A single request may be outstanding at a time. Callback lambdas are in memory; if the Activity
 * is recreated while Android's prompt is open, the new Activity should recheck/retry its action.
 * Special app access and platform-specific staged permissions need separate host flows.
 */
class PermissionRequester(activity: ComponentActivity) {
    private val launcher: ActivityResultLauncher<Array<String>> = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { coordinator.onResult() }

    private val coordinator = PermissionRequestCoordinator(
        missingPermissions = { names ->
            names.filter { name ->
                ContextCompat.checkSelfPermission(activity, name) != PackageManager.PERMISSION_GRANTED
            }
        },
        launch = { names -> launcher.launch(names.toTypedArray()) },
    )

    /**
     * Builds a request for [permissions]. The [onGranted] action runs only after all are granted.
     * The request starts at the terminal infix [PendingPermissionRequest.onDenied] call, which
     * supplies the missing-permission handler before Android can deliver a result.
     */
    fun requestPermissions(
        vararg permissions: String,
        onGranted: () -> Unit,
    ): PendingPermissionRequest = PendingPermissionRequest { onDenied ->
        coordinator.start(permissions.toList(), onGranted, onDenied)
    }
}

/** Pending, not yet launched request. Call [onDenied] once to attach the handler and start it. */
class PendingPermissionRequest internal constructor(
    private val start: ((List<String>) -> Unit) -> Unit,
) {
    private var started = false

    /**
     * Starts the request. [handler] receives the still-missing permissions after the result;
     * it also handles a refusal without an Android dialog (for example, a blocked prompt).
     */
    infix fun onDenied(handler: (List<String>) -> Unit) {
        check(!started) { "This permission request has already started" }
        started = true
        start(handler)
    }
}

internal class PermissionRequestCoordinator(
    private val missingPermissions: (List<String>) -> List<String>,
    private val launch: (List<String>) -> Unit,
) {
    private data class Pending(
        val names: List<String>,
        val onGranted: () -> Unit,
        val onDenied: (List<String>) -> Unit,
    )

    private var pending: Pending? = null

    fun start(names: List<String>, onGranted: () -> Unit, onDenied: (List<String>) -> Unit) {
        require(names.isNotEmpty()) { "At least one permission is required" }
        require(names.all { it.isNotBlank() }) { "Permissions must not be blank" }
        check(pending == null) { "A permission request is already in progress" }
        val unique = names.distinct()
        val missing = missingPermissions(unique)
        if (missing.isEmpty()) {
            onGranted()
            return
        }
        pending = Pending(unique, onGranted, onDenied)
        try {
            launch(missing)
        } catch (error: Throwable) {
            pending = null
            throw error
        }
    }

    fun onResult() {
        val request = pending ?: return
        pending = null
        val missing = missingPermissions(request.names)
        if (missing.isEmpty()) request.onGranted() else request.onDenied(missing)
    }
}
