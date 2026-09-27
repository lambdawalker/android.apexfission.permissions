# Android runtime permissions for Compose

A Compose library for explaining and requesting a set of Android runtime permissions. The main `HandlePermissions` entry point requests the set together after one button tap. Android may show several system prompts and grant only some permissions.

## Install

Once the first release has been published on Maven Central, use:

```kotlin
// settings.gradle.kts: ensure mavenCentral() is in dependencyResolutionManagement repositories
dependencies {
    implementation("com.apexfission.android.permission:core:0.1.0")
}
```

For a source checkout, include the module and use:

```kotlin
dependencies { implementation(project(":permission")) }
```

The module is `:permission` (namespace `com.apexfission.android.permission`, minSdk 24). The published version is chosen when the release workflow runs; replace `0.1.0` above with the version actually published. Declare every requested permission in your app manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Request a group

The following is a complete Activity example. The permissions are checked on composition; the system request starts only after the user taps the action button.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HandlePermissions(
                    permissions = listOf(
                        PermissionDescription(
                            permission = Manifest.permission.CAMERA,
                            label = "Camera",
                            icon = Icons.Default.PhotoCamera,
                        ) {
                            DefaultPermissionPage(
                                label = "Camera",
                                icon = Icons.Default.PhotoCamera,
                                title = "Scan documents",
                                body = "Allow camera access when you start a scan.",
                            )
                        },
                        PermissionDescription(
                            permission = Manifest.permission.RECORD_AUDIO,
                            label = "Microphone",
                            icon = Icons.Default.Mic,
                        ), // localized default page
                    ),
                    overview = PermissionOverview(page = {
                        DefaultPermissionPage(
                            label = "Access",
                            icon = Icons.Default.Lock,
                            title = "Prepare your scan",
                            body = "Review the access needed before continuing.",
                        )
                    }),
                    displayMode = PermissionDisplayMode.All, // or MissingOnly
                    overviewMode = PermissionOverviewMode.Automatic, // or Show / Hide
                    onBack = { finish() },
                    onNotNow = { finish() },
                ) {
                    Text("Ready to scan")
                }
            }
        }
    }
}
```

Imports: `android.Manifest`, `android.os.Bundle`, `androidx.activity.ComponentActivity`, `androidx.activity.compose.setContent`, Compose Material icons (`Icons`, `PhotoCamera`, `Mic`, `Lock`), `MaterialTheme`, `Text`, and the public types from `com.apexfission.android.permission`. See [the runnable sample](app/src/main/java/com/apexfission/android/permissions/MainActivity.kt) for an expanded version with its own success screen.

By default, multiple visible permissions start on a general feature overview and then show one page per permission. A single visible permission starts on its permission page. `overviewMode = PermissionOverviewMode.Show` includes the overview even for one permission; `PermissionOverviewMode.Hide` omits it even for several. `Automatic` (the default) follows the number of visible permissions. Supplying `overview` customizes its content; `overviewMode` decides whether it appears.

`PermissionDescription { ... }` and `PermissionOverview(page = { ... })` each own a full carousel page: hero and explanation can be composed together. `DefaultPermissionPage` supplies both default hero and text in a single call. If `page` is omitted, the library uses localized default copy and imagery; older `hero` and `description` properties remain supported. Set `icon` and `label` on `PermissionDescription` so the persistent selector and accessibility have stable metadata.

The icon strip stays above the primary button while pages swipe. It scrolls to center the selected permission; on the overview, the whole icon set is centered, with extra icons clipped equally at either side of the screen. The button requests the permissions as one batch regardless of the carousel page.

`displayMode = PermissionDisplayMode.All` (the default) keeps granted permissions in the carousel and marks their icons with a green check badge; selecting one still highlights its icon. `PermissionDisplayMode.MissingOnly` removes granted permissions from both the carousel and icon strip. In `Automatic` overview mode, the overview appears only while more than one **visible** permission remains. After a grant changes the visible set, the carousel starts on the overview if present, otherwise the first remaining permission. Both modes still gate protected content on every original permission and use the same batch request. The setting applies to the built-in UI; custom `permissionContent` receives all controllers.

`content` appears only while every permission is granted. On a partial grant, the screen remains and the button requests remaining permissions; if none can prompt, it opens app settings. The `PermanentlyDenied` status is inferred from request history and Android's rationale signal, not a definitive platform flag. The request callback can be observed with `HandlePermissionBundle(onPermissionsResult = { ... })` when needed.

For a single permission, the same API skips the overview page by default; use `overviewMode = PermissionOverviewMode.Show` to include it. `HandleCameraPermission` and `HandlePermissionsIndividually` remain available for older or intentionally sequential integrations. `PermissionScreen` and `PermissionBundleScreen` can render host-managed statuses and actions without launching requests themselves. The `permissionContent` parameter of `HandlePermissions` preserves its existing controller-driven custom UI path.

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
    } otherwise { missing ->
        permissionLauncher.launch(missing.toTypedArray())
    }
}
```

This is a `Context` extension, so the **check** works in any class holding a valid `Context`. The **request** still needs an Activity, Fragment, or Compose-owned result launcher and an appropriate lifecycle. The check is synchronous; requesting is asynchronous. Recheck after a successful callback, and handle denial separately to avoid repeatedly launching the prompt. This helper does not render an explanation or manage special app access.

## Screenshots and checks

Run `./gradlew :permission:testDebugUnitTest :permission:updateDebugScreenshotTest :permission:generatePomFileForMavenPublication :app:assembleDebug`. [GitHub Actions](.github/workflows/render-permission-screens.yml) renders Compose previews, checks the publication POM, and uploads the PNGs. The previews do not exercise Android system permission prompts; validate grant and denial paths on a device or emulator.

## Publish to Maven Central

The `:permission` module uses the Vanniktech Maven Publish plugin to produce a signed Android AAR, sources, Javadoc jar, and POM under `com.apexfission.android.permission:core:<version>`. The [manual release workflow](.github/workflows/publish-permission.yml) runs tests, builds the sample, then stages a signed deployment. It does not click Publish in Central Portal for you.

Setup:

1. Create a [Central Portal](https://central.sonatype.com/) account and verify ownership of the `com.apexfission.android.permission` namespace (or an accepted parent). If your registered namespace differs, edit the group in `permission/build.gradle.kts` and the install coordinates above before releasing.
2. Generate a Central Portal **user token** from Account. Store its username and password as GitHub Actions secrets `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`; the account sign-in password is not the publishing token.
3. Generate a password-protected GPG key, publish its public key to a keyserver, and export its ASCII-armored **private** key with `gpg --export-secret-keys --armor <KEY_ID>`. Store the complete armored block as `SIGNING_IN_MEMORY_KEY` and its passphrase as `SIGNING_IN_MEMORY_KEY_PASSWORD` GitHub Actions secrets. Keep the private key and passphrase out of the repository.
4. In repository Settings → Environments, create `maven-central`. Add required reviewers if you want a separate approval before uploading. Put the four secrets in that environment or at repository level.
5. In Actions → **Publish permission library** → **Run workflow**, select `main` and enter a new version such as `0.1.0`. The workflow accepts a version only on `main` and uploads to Central Portal. Inspect and **Publish** the validated deployment under Central Portal → Deployments. Each release version must be unique; update the README install version after publishing.

To verify the publication locally without uploading, run `./gradlew :permission:publishToMavenLocal -PpermissionVersion=0.1.0` with signing credentials configured. Android system permission prompts still require device or emulator testing.

See [the bundle guide](docs/batch-request.md), [AGENTS.md](AGENTS.md), and [LICENSE](LICENSE).
