# Android runtime permissions for Compose

Explain and request Android runtime permissions with a Compose gate, custom pages, or Activity-owned callback helpers. One action requests a group; Android may show several dialogs and grant only part of it. Only required permissions gate protected content.

**[Human documentation](https://lambdawalker.github.io/android.apexfission.permissions/)** · **[Agent entry point](docs/agents/index.md)** · **[Installation](IMPORT.md)** · **[Runnable demos](https://lambdawalker.github.io/android.apexfission.permissions/demos/)**

These source guides describe `main` and may contain APIs newer than the published artifact. Read [IMPORT.md](IMPORT.md) for confirmed coordinates and the published version. Contributor instructions are in [AGENTS.md](AGENTS.md).

## Screenshots

| Overview | Permission detail | Settings recovery |
| --- | --- | --- |
| <img src="docs/screenshots/bundle-overview.png" width="220" alt="Camera and microphone overview"> | <img src="docs/screenshots/permission-detail.png" width="220" alt="Selected camera permission"> | <img src="docs/screenshots/settings-recovery.png" width="220" alt="Cautious Settings recovery action"> |

These are library-owned Compose previews, not Android system dialogs. See the [full gallery](https://lambdawalker.github.io/android.apexfission.permissions/gallery/) and [capture provenance and refresh commands](docs/screenshots/README.md).

## Install

Follow [IMPORT.md](IMPORT.md) for the confirmed dependency, repository setup, and supported installation formats. Android minSdk 24 and a Compose-enabled host are required. Declare requested permissions in the host manifest. Repository demos intentionally use `implementation(project(":permission"))`.

## Request a group

Start with the [complete first-request example](https://lambdawalker.github.io/android.apexfission.permissions/getting-started/) or its [agent Markdown](docs/agents/quickstart.md). The example is extracted from a demo Activity and compiled with the sample app. It includes imports, manifest instructions, host insets, and expected grant/denial behavior.

For multiple permissions, optional access, Settings recovery, and customization, follow the [Compose guide](https://lambdawalker.github.io/android.apexfission.permissions/compose/). Requests begin after a user action, never merely because the screen is composed. Permission grants can be revoked; recheck before protected work.

### Replace the default page with your own Compose UI

Each description accepts a complete composable page. Use `DefaultPermissionPage` for a ready-made layout or provide your own UI. See [page selection and complete examples](https://lambdawalker.github.io/android.apexfission.permissions/compose/#choose-a-page-layout) and the [carousel demo source](app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt).

### Optional timed carousel

Autoplay is opt-in, with per-page reading delays and pause/resume behavior. See [timed carousel](https://lambdawalker.github.io/android.apexfission.permissions/compose/#timed-carousel).

### Required and optional access

Only required grants block protected content. An all-optional list proceeds immediately. See [required and optional permissions](https://lambdawalker.github.io/android.apexfission.permissions/compose/#required-and-optional-permissions).

### Platform-aware recipes

Location, background access, notifications and the photo picker have platform-specific flows. Use the [platform recipes](https://lambdawalker.github.io/android.apexfission.permissions/recipes/) and runnable demo; special app access is outside the ordinary runtime-permission gate.

## Code-only request with callbacks

Use an Activity-owned `PermissionRequester` when your app supplies its own UI. Registration timing, terminal `onDenied`, and callback lifetime are explained in the [callback guide](https://lambdawalker.github.io/android.apexfission.permissions/callbacks/).

## Code-only permission check

`runIfPermissionsGranted` performs a synchronous snapshot check. The host owns request launch and recovery. See the [callback guide](https://lambdawalker.github.io/android.apexfission.permissions/callbacks/) and [API contracts](https://lambdawalker.github.io/android.apexfission.permissions/reference/).

## Screenshots and checks

```bash
cd sites
npm ci
npm run check
```

This validates and builds the documentation; it does not render Android. For Android builds, device tests and explicit screenshot render/check/update commands, see [development](https://lambdawalker.github.io/android.apexfission.permissions/development/) and [AGENTS.md](AGENTS.md).

The website also contains [concepts and ownership](https://lambdawalker.github.io/android.apexfission.permissions/concepts/), [limitations](https://lambdawalker.github.io/android.apexfission.permissions/limitations/), [troubleshooting](https://lambdawalker.github.io/android.apexfission.permissions/troubleshooting/), and [migration](https://lambdawalker.github.io/android.apexfission.permissions/migration/). See [LICENSE](LICENSE) for Apache-2.0 terms.
