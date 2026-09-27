# Android runtime permissions for Compose

A Compose library for explaining and requesting a set of Android runtime permissions. The main `HandlePermissions` entry point requests the set together after one button tap. Android may show several system prompts and grant only some permissions.

## Install

```kotlin
dependencies { implementation(project(":permission")) }
```

The module is `:permission` (`com.apexfission.android.permission`, minSdk 24). Declare every requested permission in your app manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Request a group

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
            label = "Camera",                 // selector and accessibility metadata
            icon = Icons.Default.PhotoCamera, // selector icon
        ) {
            DefaultPermissionPage(
                label = "Camera",
                icon = Icons.Default.PhotoCamera,
                title = "Scan documents",
                body = "Allow camera access to capture a document when you start a scan.",
            )
        },
        PermissionDescription(permission = Manifest.permission.RECORD_AUDIO) {
            MyAudioPage() // hero and explanation together
        },
    ),
    overview = PermissionOverview(page = {
        FeatureOverviewPage() // full carousel page
    }),
    modifier = Modifier.padding(innerPadding),
    onBack = { finish() },
    onNotNow = { finish() },
) {
    Greeting(name = "Permissions granted")
}
```

With multiple permissions, the carousel starts on a general feature overview and then shows one page per permission. `PermissionDescription { ... }` and `PermissionOverview(page = { ... })` each own a full carousel page: hero and explanation can be composed together. `DefaultPermissionPage` supplies both default hero and text in a single call. If `page` is omitted, the library uses localized default copy and imagery; older `hero` and `description` properties remain supported. Set `icon` and `label` on `PermissionDescription` so the persistent selector and accessibility have stable metadata.

The icon strip stays above the primary button while pages swipe. It scrolls to center the selected permission; on the overview, the whole icon set is centered, with extra icons clipped equally at either side of the screen. The button requests the permissions as one batch regardless of the carousel page.

`content` appears only while every permission is granted. On a partial grant, the screen remains and the button requests remaining permissions; if none can prompt, it opens app settings. The `PermanentlyDenied` status is inferred from request history and Android's rationale signal, not a definitive platform flag. The request callback can be observed with `HandlePermissionBundle(onPermissionsResult = { ... })` when needed.

For a single permission, the same API skips the overview page. `HandleCameraPermission` and `HandlePermissionsIndividually` remain available for older or intentionally sequential integrations. `PermissionScreen` and `PermissionBundleScreen` can render host-managed statuses and actions without launching requests themselves. The `permissionContent` parameter of `HandlePermissions` preserves its existing controller-driven custom UI path.

The launcher handles ordinary Android runtime permissions. Special app access (exact alarms, overlay, all-files access) and runtime permissions with platform-specific sequencing (such as background location) need their own host flow. Do not assume the batch is atomic or that Android will present exactly one system dialog.

## Code-only request with callbacks

For a flow with no library screen, create a `PermissionRequester` as an Activity property so the Android result launcher is registered before the Activity starts. The infix `onDenied` call is **terminal**: it attaches the handler and starts the request. The library checks current grants first, then rechecks after Android returns. One request may be outstanding at a time.

```kotlin
class ScannerActivity : ComponentActivity() {
    private val permissionRequester = PermissionRequester(this)

    private fun startScanner() = with(permissionRequester) {
        requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ) {
            openScanner() // all granted now, or granted after the prompt
        } onDenied { missing ->
            showDeniedState(missing)
        }
    }

    // Call startScanner() from a user action such as a button tap.
    private fun openScanner() { /* use the protected feature */ }
    private fun showDeniedState(missing: List<String>) { /* host UI or settings route */ }
}
```

`requestPermissions(...) { ... }` only builds the pending call. The prompt starts when `onDenied` is attached, so even an immediate result has both callbacks ready. If all permissions are already granted, `openScanner()` runs synchronously at that terminal call. The callbacks are held in memory; after Activity recreation or process death, recheck and retry the user action rather than assuming an old callback will continue. The requester supports ordinary runtime permissions, not special app access or permissions that need staged platform flows.

## Code-only permission check

Use `runIfPermissionsGranted` when the host wants to handle its own request flow without a library screen. It accepts either varargs or a `List<String>` and returns the missing permission names to `otherwise`:

```kotlin
private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { grants ->
    if (grants.isNotEmpty() && grants.values.all { it }) startFeature() // recheck
    else showDeniedState()
}

private fun startFeature() {
    runIfPermissionsGranted(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    ) {
        doSomething()
    }.otherwise { missing ->
        permissionLauncher.launch(missing.toTypedArray())
    }
}
```

This is a `Context` extension, so the **check** works in any class holding a valid `Context`. The **request** still needs an Activity, Fragment, or Compose-owned result launcher and an appropriate lifecycle. The check is synchronous; requesting is asynchronous. Recheck after a successful callback, and handle denial separately to avoid repeatedly launching the prompt. This helper does not render an explanation or manage special app access.

## Screenshots and checks

Run `./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :app:assembleDebug`. [GitHub Actions](.github/workflows/render-permission-screens.yml) renders Compose previews and uploads the PNGs. The previews do not exercise Android system permission prompts; validate grant and denial paths on a device or emulator.

See [the bundle guide](docs/batch-request.md), [AGENTS.md](AGENTS.md), and [LICENSE](LICENSE). This repository does not publish a Maven artifact.
