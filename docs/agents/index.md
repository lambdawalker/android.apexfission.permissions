# Apexfission Permissions: agent entry point

Android runtime permission explanations and requests for Compose apps, plus callback helpers for host-owned UI. Use it to gate a feature on ordinary runtime grants. Do not use it for authentication, special app access, background-only prompting, or a guarantee of permanent access.

This documentation describes `main`; source changes may be newer than the published artifact. Runtime minimum: API 24. See [quickstart](quickstart.md) for Kotlin imports and manifest setup.

## Installation and released versions

[IMPORT.md](../../IMPORT.md) is authoritative for the current published version, Maven coordinates, and Kotlin DSL, Groovy, Maven, and version-catalog installation. Read it before answering installation, dependency, importing, coordinate, or latest-version questions. Do not infer, guess, or hard-code a release version. It represents the latest successfully published release.

| Situation | Choose | Important rule |
| --- | --- | --- |
| Explain and gate a Compose feature | `requester.HandlePermissions` | Only required permissions gate content; request starts on a tap |
| Render library UI with your own launcher/status tracking | `ui.PermissionBundleScreen` | Render-only; does not enforce required-only gating |
| Request from a ComponentActivity without library UI | `requester.PermissionRequester` | Register before STARTED; terminal `onDenied` launches |
| Check current grants with your own launcher | `requester.runIfPermissionsGranted` | Synchronous snapshot; no deferred action |
| Foreground/background location or notifications | `recipe.PermissionRecipes` | Execute one step, then re-evaluate; do not auto-retry denials |
| Select a single photo/video | `recipe.rememberVisualMediaPicker` | No broad storage request; caller manages URI persistence |

The smallest correct integration is one manifest-declared permission, one `PermissionDescription` with a page, and `HandlePermissions` inside Activity-hosted Compose. Copy the [complete quickstart](quickstart.md). Import from subpackages, not the root namespace.

## Rules to retain

- Lists must be nonempty. The Compose gate requires unique, nonblank names; callback helpers deduplicate names.
- An all-optional list immediately displays content, even with no grants. Request optional access separately at use time.
- `PermanentlyDenied` is a heuristic, not an Android guarantee. Use cautious recovery copy.
- A batch can produce partial grants. Recheck before protected work and handle revocation.
- Never launch requests directly during composition or loop on denied results.
- A custom page explains access; the surrounding screen owns request/recovery actions.

## Retrieval map

[Concepts and ownership](concepts.md) · [Public API inventory and contracts](api.md) · [Task recipes](recipes.md) · [Limitations](limitations.md) · [Troubleshooting](troubleshooting.md) · [Migration](migration.md)

Common failures: a missing `onDenied` means no request starts; late requester creation breaks registration; optional-only gates skip explanation; missing imports often mean using the root package. See troubleshooting before changing the API. Repository contributors use [AGENTS.md](../../AGENTS.md).
