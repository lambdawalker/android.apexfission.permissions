# README screenshots

The seven accepted PNGs were rendered by the [screenshot workflow](https://github.com/lambdawalker/android.apexfission.permissions/actions/runs/40774456236) at commit `a3638a937244d05b13cdb0810360ac8037530544`, downloaded from its `permission-screen-images` artifact (`12854851431`), visually reviewed, and accepted with `screenshots:update`. The capture input hash matches the current library, fixture, and toolchain inputs. The manifest records the original capture environment and workflow provenance. No images were manually composed or edited.

| File | `PermissionBundleScreenshotTest` preview |
| --- | --- |
| `bundle-overview.png` | `firstPage` |
| `permission-detail.png` | `secondPage` |
| `settings-recovery.png` | `partialDenial` |
| `single-permission-overview.png` | `singlePermissionOverview` |
| `multiple-without-overview.png` | `multiplePermissionsNoOverview` |
| `overflowing-icons.png` | `overflowOverview` |
| `segmented-reading-progress.png` | `autoplayReadingProgress` |

These captures include the layout spacing change from `77233fd`. `single-permission-overview.png` correctly maps to `singlePermissionOverview` (two pages, including the overview). A no-overview single permission is a separate scenario; do not replace or relabel the overview image to represent it.

Use the automated commands below to refresh images after UI changes. Static previews do not capture Android system prompts.

## Automated mapping, comparison, and intentional updates

`manifest.json` is the machine-readable mapping of stable filenames to preview methods, capture configuration, captions, hashes, and provenance. Historical images retain their original workflow evidence; missing historical input hashes are explicitly null. No fresh render is implied by adding the manifest.

From `sites/`, after `npm ci`:

```bash
npm run screenshots:manifest # validate committed PNGs and recorded hashes only
npm run screenshots:render   # Gradle renders candidates; writes capture.json provenance
npm run screenshots:check    # compare decoded pixels with accepted documentation images
npm run screenshots:update   # intentional acceptance AFTER visually reviewing candidates
npm run check               # verify docs and build/check the human site
```

The Gradle update task is used only to render into its working reference directory. That directory is not the accepted documentation baseline. Ordinary CI compares candidates against committed `docs/screenshots/*.png`; it never commits or accepts changed images. Comparison ignores PNG compression differences but requires identical dimensions and pixels. Missing or ambiguous preview outputs fail rather than copying an arbitrary file.

The update command exports all seven mapped scenarios to stable filenames and records their hashes and capture provenance. Commit the manifest and reviewed PNGs together. Do not change image bytes by hand. The `capture.json` input hash must match the current source/toolchain inputs; a source commit alone is insufficient evidence for an uncommitted working tree.

For remote rendering, run **Render permission screens** with **candidates_only** enabled. Download the `permission-screen-images` artifact and locate its `reference/` directory containing `capture.json`. At the matching source inputs, pass that absolute directory to `npm run screenshots:check -- /path/to/reference` or, after review, `npm run screenshots:update -- /path/to/reference`. CI artifacts preserve candidates and comparison evidence even when differences fail validation.

UI/resource/fixture/toolchain changes trigger rendering in PR CI. Site-only edits validate existing images without starting Android. Ordinary release preflight renders and compares the exact release source before upload; rerunning a site deployment reuses reviewed images. All captures remain component previews, not evidence of Android system dialogs.
