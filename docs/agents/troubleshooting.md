# Troubleshooting

| Symptom | Diagnose / likely cause | Correct fix; avoid |
| --- | --- | --- |
| No request after `requestPermissions` | Pending builder was never completed | Attach `onDenied` exactly once; do not store and reuse pending calls |
| Registration exception | Requester constructed after Activity STARTED | Construct as an Activity property, not inside a click handler |
| “request already in progress” | Second terminal call while a request is pending | Disable duplicate actions; do not create extra requesters to bypass coordination |
| “already started” | Same pending object reused | Build a new call for a later user action |
| Optional page never appears | Required grants already exist or every item is optional | Request optional access at point of use; do not make it required merely to force onboarding |
| Empty/duplicate/blank list exception | Invalid gate configuration | Supply nonempty unique nonblank names; helper requests deduplicate but still reject empty/blank inputs |
| No dialog / repeated denial | Missing manifest declaration, unsupported OS permission, denial history or policy | Inspect merged host manifest and current grant/rationale; offer cautious recovery, never auto-loop |
| Settings returned but feature still unavailable | Access was not enabled or host-managed state is stale | Recheck grant/recipe on resume; opening Settings is not success |
| Callback lost after rotation | Callbacks belonged to destroyed Activity | Restore host intent/state and recheck on the next user action; do not retain old Activity |
| Import unresolved | Root namespace used instead of subpackage, or obsolete API | Use [API inventory](api.md) and [migration](migration.md) |
| Maven dependency unresolved | Wrong coordinates/version or repository missing | Match `com.apexfission.androi:permission`, enable Maven Central and verify the chosen publication exists; do not silently correct the group spelling |
| Unexpected screen inset | Host and container padding combined incorrectly | Inspect actual layout and apply insets exactly once; current bundle source does not call `systemBarsPadding` |
| Bitmap crashes or stale artwork | Host recycled/mutated an image while composed | Keep artwork valid and stable until no consumer uses it; library does not take ownership |
| Approximate grant after precise request | User selected coarse access | Inspect actual accuracy and degrade or explain; never claim fine access from coarse grant |
| Notification recipe keeps requesting | Recipe has no denial-history state | Handle denial in host UI and allow a deliberate Settings route |
| Build fails before compilation | SDK/toolchain unavailable | Follow [AGENTS.md](../../AGENTS.md); distinguish Java 17 bytecode from the Java 25 Gradle daemon |

Incorrect: call `picker.launch()` or a request terminal directly in a composable body. Correct: register/remember during composition and invoke from `Button(onClick = { ... })`.

Incorrect: execute camera work inside `content` as an ordinary function call on every recomposition. Correct: use lifecycle-aware resource APIs/effects and release the host's resources on disposal.

Before shipping an integration, exercise first grant, partial grant, denial, Settings return, revocation, recreation and optional denial. For media, test cancellation and URI lifetime. See [compiled demos](recipes.md) and [repository verification](../../AGENTS.md#verification).
