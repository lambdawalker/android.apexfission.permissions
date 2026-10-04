# Code-only integration

For staged location, notifications, or user-selected media, see [platform-aware recipes](platform-recipes.md) before using a generic permission batch.

Use these APIs when the host owns the UI. Declare every permission in its app manifest. Both paths target ordinary runtime permissions; special app access and staged platform requests need another flow.

## Library-owned launcher

Construct `PermissionRequester` as an Activity property **before STARTED** and call it from a user action:

```kotlin
class ScannerActivity : ComponentActivity() {
    private val permissions = PermissionRequester(this)

    private fun onScanClicked() = with(permissions) {
        requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ) {
            startScan() // all granted, possibly synchronously
        } onDenied { missing: List<String> ->
            showDeniedState(missing)
        }
    }

    private fun startScan() { /* protected work */ }
    private fun showDeniedState(missing: List<String>) { /* host UI or settings */ }
}
```

`requestPermissions(...) { onGranted }` creates a pending call; infix `onDenied` **attaches the callback and starts it**. The requester checks current grants, launches only distinct missing permissions, and rechecks after Android returns. Only one request can be outstanding. Never reuse a pending call. Callbacks live in memory; after Activity recreation or process death, recheck on the next user action.

## Host-owned launcher

`Context.runIfPermissionsGranted` is synchronous, with vararg and `List<String>` overloads. It never presents a dialog or retains the action. Example inside an Activity:

```kotlin
private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { grants ->
    if (grants.isNotEmpty() && grants.values.all { it }) {
        onFeatureClicked() // recheck current grants
    } else {
        showDeniedState()
    }
}

private fun onFeatureClicked() {
    runIfPermissionsGranted(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    ) {
        startFeature()
    } otherwise { missing ->
        permissionLauncher.launch(missing.toTypedArray())
    }
}
```

`otherwise` receives distinct missing names from **that check**. Handle denial separately; do not retry from a denied result or the prompt may reopen repeatedly. When already granted, the action runs synchronously. Checking works with any valid `Context`, while requesting needs an Activity, Fragment, or Compose result launcher registered at the appropriate lifecycle point.

For library-owned UI see [Compose integration](compose.md).
