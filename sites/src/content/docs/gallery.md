---
title: Screen gallery
description: Rendered Compose states from the library screenshot fixtures.
---

These images come from the library's Compose screenshot tests. They show the explanation UI; Android's system permission dialogs are device-owned and are not included.

<div class="site-gallery">
  <figure><img src="/android.apexfission.permissions/screenshots/bundle-overview.png" alt="Overview of grouped camera and microphone permissions" loading="lazy" /><figcaption>Bundle overview</figcaption></figure>
  <figure><img src="/android.apexfission.permissions/screenshots/permission-detail.png" alt="A selected permission with its icon highlighted" loading="lazy" /><figcaption>Permission detail</figcaption></figure>
  <figure><img src="/android.apexfission.permissions/screenshots/settings-recovery.png" alt="Settings action after access might no longer prompt" loading="lazy" /><figcaption>Settings recovery</figcaption></figure>
  <figure><img src="/android.apexfission.permissions/screenshots/single-permission-overview.png" alt="Optional overview before a single permission" loading="lazy" /><figcaption>Single-permission overview</figcaption></figure>
  <figure><img src="/android.apexfission.permissions/screenshots/multiple-without-overview.png" alt="Several permissions with overview hidden" loading="lazy" /><figcaption>Overview hidden</figcaption></figure>
  <figure><img src="/android.apexfission.permissions/screenshots/overflowing-icons.png" alt="Horizontally scrolling icon strip with eight permissions" loading="lazy" /><figcaption>Scrollable icons</figcaption></figure>
  <figure><img src="/android.apexfission.permissions/screenshots/segmented-reading-progress.png" alt="Segmented reading indicator with the current page in progress" loading="lazy" /><figcaption>Reading progress</figcaption></figure>
</div>

## How these images are made

The committed PNGs are direct outputs of `PermissionBundleScreenshotTest` in `:permission`. They are copied into the site's `public/screenshots/` directory at build time, so the gallery and repository README display the same stable fixtures.

| Image | Screenshot preview |
| --- | --- |
| `bundle-overview.png` | `firstPage` |
| `permission-detail.png` | `secondPage` |
| `settings-recovery.png` | `partialDenial` |
| `single-permission-overview.png` | `singlePermissionOverview` |
| `multiple-without-overview.png` | `multiplePermissionsNoOverview` |
| `overflowing-icons.png` | `overflowOverview` |
| `segmented-reading-progress.png` | `autoplayReadingProgress` |

Refresh them with `./gradlew :permission:updateDebugScreenshotTest` or the [Render permission screens workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/workflows/render-permission-screens.yml). Copy the matching PNGs from `permission/src/screenshotTestDebug/reference/` or the workflow artifact to `docs/screenshots/`, review the gallery, and commit them with UI changes. These previews do not show Android's system permission dialogs; use the [device tests](../development/#device-tests) for grant and denial flows.
