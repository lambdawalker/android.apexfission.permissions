# Choose a task recipe

Use the complete executable demos as the source of integration code; the linked focused guides explain decisions and caveats. Demo functions are app code, not exported library symbols. Android CI compiles `:app`; device tests exercise selected paths, not every scenario below.

| Situation | API and why | Complete example | Expected behavior / caveat |
| --- | --- | --- | --- |
| Required camera, optional narration | `HandlePermissions` plus `required = false` | [Carousel demo](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) | Required access unlocks content; optional denial disables narration |
| Custom explanation, overview and autoplay | Page lambdas, `PermissionOverview`, reading delay | [Carousel demo](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) | Library retains actions; autoplay pauses on interaction |
| Your own UI and permission launcher | `runIfPermissionsGranted` | [Code-only demo](../../app/src/main/java/com/apexfission/android/permissions/demo/CodeOnlyDemo.kt) | Recheck after result, report denial without relaunching |
| Your own UI, library launcher | `PermissionRequester` | [Activity registration](../../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt) and [code-only demo](../../app/src/main/java/com/apexfission/android/permissions/demo/CodeOnlyDemo.kt) | `onDenied` starts request; one outstanding call |
| Staged location, notification recovery | `PermissionRecipes` | [Platform demo](../../app/src/main/java/com/apexfission/android/permissions/demo/PlatformRecipesDemo.kt) | Explain each next step and refresh after result/Settings |
| Select photo/video | `rememberVisualMediaPicker` | [Platform demo](../../app/src/main/java/com/apexfission/android/permissions/demo/PlatformRecipesDemo.kt) | URI or null; no broad media grant |
| Bitmap, Drawable, vector or resource hero | `DefaultPermissionPage` overloads | [Artwork demo](../../app/src/main/java/com/apexfission/android/permissions/demo/HeroArtworkDemo.kt) | Host owns image lifetime; strip icon is separate |
| Supply statuses without Android launcher | `PermissionBundleScreen` | [Screenshot fixtures](../../permission/src/screenshotTest/kotlin/com/apexfission/android/permission/PermissionBundleScreenshotTest.kt) | Render-only; host implements actions and required-only gating |

Detailed instructions: [Compose](compose.md), [callback helpers](code-only.md), [platform flows](platform-recipes.md). All samples use permissions declared in the [demo manifest](../../app/src/main/AndroidManifest.xml). Copy only permissions needed for your feature.
