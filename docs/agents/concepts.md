# Concepts, lifetime and ownership

## Gate versus renderer

`HandlePermissions` maps the full description list to Accompanist permission states, constructs a `PermissionGrants` snapshot, and composes content when `missingRequired` is empty. Only otherwise does it filter visible pages and render `PermissionBundleScreen`. `MissingOnly` affects presentation, not the grant check or original request set. Changing visible names or overview mode recreates the keyed screen; the gate retains its saveable autoplay-stop flag.

`PermissionBundleScreen` receives ordered statuses. Its button invokes `onComplete` only if **all supplied statuses** are Granted; otherwise it invokes `onRequest` if any status is NotRequested or RationaleRequired, or `onOpenSettings` otherwise. The `required` flag labels pages but does not change this standalone action decision. It neither queries Android nor launches a request.

## Inferred state

| Evidence, in priority order | Status |
| --- | --- |
| Currently granted | Granted |
| Android says rationale should be shown | RationaleRequired |
| Locally recorded prior request | PermanentlyDenied |
| Otherwise | NotRequested |

The gate stores per-permission booleans in application SharedPreferences named `camera_permission_state`. It marks missing entries before launch and clears history when granted. This history covers requests through this gate, not every request another component may make. Clearing/restoring app data or outside requests can affect inference. There is no definitive permanent-denial flag here.

## Ownership and execution

| Resource/work | Owner and lifetime |
| --- | --- |
| Gate permission launcher | Compose/Accompanist lifecycle; not a durable background operation |
| `PermissionRequester` | Activity instance; never retain in a singleton or ViewModel that outlives it |
| Pending requester callbacks | In memory until result; not restored across recreation/process death |
| Grant/check snapshots | Point-in-time values; recheck after external changes and before protected work |
| Page Bitmap/Drawable | Host owns validity; library does not recycle supplied bitmaps; avoid concurrent mutation or sharing a mutable Drawable between views |
| Selected media URI | Host owns opening/closing streams and any supported persistence; picker returns URI or null |
| Autoplay and scroll jobs | Composition-owned; effects cancel and lifecycle observer is removed on disposal |

Compose UI, launcher creation/launch, and requester use belong on the main thread. The requester is not synchronized. Its already-granted callback runs inline in the terminal call; Activity-result callbacks run through the Activity result mechanism on the UI thread. `runIfPermissionsGranted` and `otherwise` run inline on the calling thread with no dispatcher switch. Recipe calculations are synchronous and do not launch anything. Keep callbacks short; the library provides no worker queue or backpressure facility. Callback exceptions propagate; there is no library error callback.

The requester clears its pending slot before delivering a result, permitting a subsequent request, but do not turn denial into automatic retries. A launcher exception clears the pending slot and propagates. There is no public cancellation API. Navigating away is not a guarantee that an outstanding platform prompt is cancelled.

Autoplay is opt-in, active only while RESUMED and when touch exploration is observed disabled. User interaction latches pause until resume; unchanged pages retain elapsed progress. Page/delay/count changes recreate progress. The touch-exploration flag is read during composition, not watched by a dedicated listener. Do not promise immediate response to accessibility setting changes without recomposition.
