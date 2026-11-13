# Publishing Permissions

[IMPORT.md](../IMPORT.md) is the authoritative latest confirmed installation reference. Historical records under `docs/releases/history/` retain their original code, documentation, destination, and source-tag identities. A tag or an upload attempt alone is not a confirmed publication.

## Shared tooling

This repository uses the same Gradle plugins and Rust CLI as [Demo Common](https://github.com/lambdawalker/android.apexfission.demo-common). Their source is [gradle.android-repo-setup](https://github.com/lambdawalker/gradle.android-repo-setup). The plugin version is pinned in `gradle/libs.versions.toml`; the CLI source revision is pinned by `tooling_ref` in `.project/setup/config.yaml`. The two pins are independent: the CLI needs the configurable verification/workflow support used here, while the published plugin supplies the existing Gradle publication conventions.

Run `setup.bat` on Windows or `sh ./setup.sh` on Linux/macOS. These shared CLI-generated launchers install and verify the configured CLI, cache it under `.project/setup/cache/`, and forward arguments. Release pins use verified binaries; full commit pins require Cargo and build the exact source. Run `setup check` to validate generated files and evaluated module configuration.

## Configuration and generated files

- `.project/setup/config.yaml` selects the permission module, app module, Java 25 daemon, artifact verification contract, Zola build, and release-source validation command.
- `.project/setup/repositories.yml` registers Maven Central and JitPack with their existing matching GitHub environments. Regenerate its properties and workflow choices with `android-repo-setup publishing_config generate`.
- `android-repo-setup workflows --overwrite` regenerates managed workflows. `--check` detects drift without writing. `.project/setup/workflow-generation.json` records ownership. Review changes before committing.
- `docs/templates/MODULE_IMPORT.md.template` owns installation prose. `android-repo-setup module_release generate` and `verify` back Gradle's `generateImportDocs` and `verifyImportDocs` tasks.

The repository-owned device and curated screenshot workflows complement the generated build, release, signing, reconciliation, and documentation workflows. The shared Rust CLI and Zola render the bilingual consumer site directly. Builds require Zola and full Git history, without a Python or Astro pipeline.

## Identity and publication

The published Central group remains **`com.apexfission.androi`**, with artifact **`permission`**; the spelling is intentional. JitPack uses **`com.github.lambdawalker:android.apexfission.permissions:permission~vX.Y.Z-jitpack`** for destination-scoped releases. Older unscoped consumer versions remain as recorded. The root project name intentionally matches the GitHub repository because the plugin uses it for the JitPack artifact ID. Public Kotlin namespaces are independent of these coordinates.

Use **Publish** on `main`, selecting the destination and optionally a stable version. The shared CLI allocates or reuses the destination's release identity, reserves immutable source and pending journals, verifies publication artifacts, and advances installation records only after public confirmation. Canonical destination tags are `permission/vX.Y.Z-maven-central` and `permission/vX.Y.Z-jitpack`. Preserve older source tags and confirmation records. New build, configuration, documentation, or release-protocol inputs can require a new patch version; this migration does not publish one.

The Gradle plugin configures AAR, sources/documentation jars, reproducible archives, unsigned local verification publications, and reservation-guarded Central uploads. Only `:permission` is published. JitPack bootstraps Java 25 with SDKMAN and invokes `:permission:publishJitPack`; that task validates JitPack's canonical scoped tag and reported source before publishing to Maven Local.

Before either upload path, `android-repo-setup release-validate --source "$SOURCE_SHA"` creates a disposable worktree at the chosen source and runs the steps in `.project/verification/checks.json`. It builds the site and renders/compares reviewed screenshots there. The original release checkout remains clean for the CLI's artifact provenance checks. Validation failures stop publication and clean up the worktree.

## Recovery and environment setup

Use **SUB - Finalize Permissions release** for an interrupted publication: select the same destination and reserved version. It verifies public artifacts and completes bookkeeping without allocating another identity or uploading Maven artifacts again. Unknown, queued, or partial provider outcomes must retain their journals. For a pre-migration reservation whose source lacks the shared plugin/configuration, use its original workflow revision for recovery; this migration does not rewrite older source identities. A successful package publication followed by a site failure is recovered by rerunning **Documentation**.

Use **Reconcile failed JitPack attempts** in preview mode first. Applying it archives only a proven terminal failure for the exact source with no public artifacts; canonical source tags remain immutable. Do not remove pending journals based only on an Actions failure. For the full protocol, signing environments, vaults, propagation checks, and reconciliation guarantees, see the [shared CLI guide](https://github.com/lambdawalker/gradle.android-repo-setup#standalone-setup-cli).

The Maven environments keep their existing credentials. Generated signed APK builds use the `demo-apk-release` environment and remain manual. Environment timers, credentials, branch policies, and signing keys are not configured by this source migration; use the setup wizard when those operations are needed.

Legacy Central 0.2.1 has installation evidence in `publishing/legacy-installation.json` but no invented source snapshot. Historical failed JitPack 0.2.2 remains a failed attempt, not a publication. Confirmed history and old consumer versions are preserved.

## Credential-free verification

```bash
android-repo-setup workflows --check
android-repo-setup publishing_config check
android-repo-setup module_release verify
android-repo-docs history verify
npm --prefix sites ci
node --test sites/scripts/tests/*.test.mjs
./gradlew verifyImportDocs :permission:testDebugUnitTest :permission:lintRelease :app:assembleDebug
./gradlew :permission:publishAllPublicationsToVerificationRepository -PreleaseModule=permission -PreleaseVersion=9.8.7
# From a clean committed checkout:
android-repo-setup module_release check-local --module permission --version 9.8.7 --source "$(git rev-parse HEAD)"
```

For JitPack verification, set `RELEASE_REPOSITORY=jitpack`, add `-PjitpackBuild=true -PjitpackScopedTag=true` to the publication command, and run the verifier under the same destination. These checks create no release tags or external publication. The configured contract requires the permission package, Java 17 class files, and its exported Accompanist dependency. Screenshot and device checks still require the pinned Android toolchain.
