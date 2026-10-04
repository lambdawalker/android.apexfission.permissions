# Limitations and security boundaries

- Ordinary runtime permissions only. Overlay, all-files access, exact-alarm access and other special settings require separate host flows. The library does not validate whether an arbitrary string is a runtime permission or declared in the manifest.
- A batch is not atomic and cannot guarantee one system dialog. Android, target SDK, device policy and OEM behavior determine outcomes.
- Permission state is not authentication, document-signing authorization or a security boundary around background code. The gate only controls composition. It does not stop resources/jobs you already started; the host owns cleanup and handles revocation or `SecurityException` at protected API use.
- Location recipes inspect grants, not whether location services are enabled. Notification readiness is not a guarantee that a particular notification channel delivers alerts.
- Recipes do not track denial history. A blocked notification/foreground-location request can continue returning `RequestRuntime`; the host must provide recovery and avoid retry loops.
- Background location decisions inspect SDK and grants, not every legacy-target exception. The supplied recipe uses separate background requests on API 29 and Settings on API 30+ once foreground access is satisfied. Validate unusual target-SDK combinations separately.
- The single-item media picker is not a broad gallery-permission manager. URI persistence and full-library/selected-photo reselection belong to the host.
- All-optional gates do not show an acquisition screen. `onPermissionsResult` observes request results, not every grant change after Settings or resume.
- No request queue, timeout, public cancellation, durable callback continuation, or permission-change subscription is provided by `PermissionRequester`.
- Reading estimates count whitespace-delimited words, add two seconds, and clamp to 4–60 seconds. This is not a reading-speed guarantee. Use explicit positive delays for image-heavy or unsegmented-language text.
- Default visual labels are not a complete localization system. Override labels and host pages. No quantified maximum batch size or performance budget is established; keep requests tied to the current feature and avoid decoding large artwork during recomposition.

See [concepts](concepts.md) for ownership/threading and [platform recipes](platform-recipes.md) for the supported OS branches. Device CI currently covers API 29 and 35; it does not certify every API/OEM, real picker, or policy configuration.
