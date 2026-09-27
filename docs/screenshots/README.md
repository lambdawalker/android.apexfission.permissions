# README screenshots

The PNGs in this directory are direct copies from the `permission-screen-images` artifact of the [passing screenshot workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/runs/36343189239) at commit `d7f8c9e`. No images were manually composed or edited.

| File | `PermissionBundleScreenshotTest` preview |
| --- | --- |
| `bundle-overview.png` | `firstPage` |
| `permission-detail.png` | `secondPage` |
| `settings-recovery.png` | `partialDenial` |
| `single-permission-overview.png` | `singlePermissionOverview` |
| `multiple-without-overview.png` | `multiplePermissionsNoOverview` |
| `overflowing-icons.png` | `overflowOverview` |

To refresh them after changing the UI, run `./gradlew :permission:updateDebugScreenshotTest` or trigger [Render permission screens](../../.github/workflows/render-permission-screens.yml). Copy the matching PNGs from `permission/src/screenshotTestDebug/reference/` or from the workflow artifact to these stable filenames, check the README gallery, and commit them with the UI changes. Static previews do not capture Android's system prompts.
