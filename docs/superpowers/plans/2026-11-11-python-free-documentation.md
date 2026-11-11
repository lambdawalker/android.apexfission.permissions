# Python-free documentation implementation plan

Goal: keep the existing Astro documentation and Pages deployment working using Rust and Node.

Architecture: call the pinned Rust CLI with an explicit project path from the current Astro renderer. Preserve documentation_command, screenshot validation, immutable release worktrees, and generated workflow ownership.

- [x] Replace Python fixtures with real Rust CLI archive/export integration tests; verify they fail without the bridge.
- [x] Call `android-repo-setup --project <root> documentation_history export` and remove the compatibility shim. Update maintainer guidance.
- [x] Add a Windows batch entry point using the current source-pinned PowerShell bootstrap; preserve argument and exit-code forwarding. Keep the working source pin until a binary release is published.
- [x] Correct obsolete Python command recommendations in Rust tooling and cover the archive recovery message.
- [x] Run the complete site checks, setup/release checks, Rust tests, and review diffs. Prepare reviewable pull requests.

Release constraint: retain the existing Permissions source pin until a published CLI release contains its required workflow and verification changes. Switching to downloaded binaries follows that publication. Never pin an unpublished tag.
