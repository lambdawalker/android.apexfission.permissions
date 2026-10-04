# Release and installation documentation

[IMPORT.md](../IMPORT.md) is the authoritative current published version and
installation reference. It is generated from
[docs/templates/IMPORT.md.template](templates/IMPORT.md.template). Usage guides
link to it; do not maintain independent versioned dependency declarations.

## Ownership and local work

- Stable `vX.Y.Z` Git tags record release history and identify the source actually
  built. The next release increments the highest semantic version's patch number;
  it does not depend on workflow run numbers or tag creation dates.
- `GROUP` and `POM_ARTIFACT_ID` in `gradle.properties` feed the sole Maven module,
  `:permission`, and the documentation renderer. The demo keeps its local project dependency.
- `-PreleaseVersion=X.Y.Z` supplies the version to the Maven publication. Builds
  without it use a development snapshot; Central tasks require an explicit stable
  version. The old `permissionVersion` override is removed.
- Edit the Markdown template, then run `./gradlew generateImportDocs verifyImportDocs`.
  Without an override, generation reads the recorded version in `IMPORT.md`, so
  editing prose does not announce an unpublished release. Python 3 is required.
- After confirming an actual publication, the explicit form is
  `./gradlew generateImportDocs -PreleaseVersion=X.Y.Z`. Do not use a proposed
  next version in committed docs before publication succeeds.
- CI runs the renderer's unit/integration tests and `verifyImportDocs`, regenerates
  the file to check determinism, and checks the Gradle-generated POM version.
  The verification task detects template/coordinate drift without consulting
  Central. The release workflow additionally checks actual Central availability.

There were no release tags when this system was introduced. The initial generated
file was seeded from Maven Central's public metadata and artifact, not the old
build fallback. Until the first stable tag exists, the workflow bootstraps from
Central's highest stable published version. It does not invent historical tags
whose source provenance is unknown. Afterward tags control progression; Central
being ahead of tags or missing a tagged version stops the workflow for investigation.
Prerelease tags are ignored. This workflow intentionally supports patch releases;
major/minor release policy must be explicitly extended before using it for those.

## Publish

1. Ensure the namespace shown in IMPORT.md is verified in Central Portal.
2. In the GitHub `maven-central` environment configure `MAVEN_CENTRAL_USERNAME`,
   `MAVEN_CENTRAL_PASSWORD`, `SIGNING_IN_MEMORY_KEY`, and, if encrypted,
   `SIGNING_IN_MEMORY_KEY_PASSWORD`. Use a Portal user token and an armored GPG
   private key. Keep keys out of Git.
3. Run **Publish permission library** on `main`, leaving `resume_version` empty.
   No version input is needed for an ordinary release.

The serialized workflow checks out current `main` with full history/tags, selects
and records the source SHA, runs unit/build checks, then pushes a
`release-pending/X.Y.Z` reference **before** attempting publication. That reference
is an attempt journal, not a released-version tag. It blocks another release if
Central succeeds but the runner loses its response, times out, or cannot update Git.

The workflow runs `:permission:publishAndReleaseToMavenCentral` with the selected
`releaseVersion`. Unlike the previous staging-only task, this requests automatic
publication. It then waits up to 40 minutes for the matching public POM and AAR.
Only after confirmation does it generate/verify IMPORT.md. It commits only that
file if changed, using the Actions bot identity. One atomic push advances main,
creates `vX.Y.Z` at the original artifact source SHA, and removes the pending tag.
A concurrent main update or protected-branch refusal fails without force-pushing.
Installation/build input changes during publication also stop finalization for review.

The workflow needs `contents: write`; repository rules must permit the bot's docs
commit and tag writes. It does not get branch-protection bypass credentials.
A successful publish workflow triggers the Pages workflow with `workflow_run`,
since commits made with `GITHUB_TOKEN` do not trigger normal push workflows. Pages
renders and serves IMPORT.md directly from its committed source during site build.

## Failure and recovery

A failed upload, rejected Central validation, timeout, or failed docs generation
never advances committed IMPORT.md. A pending tag intentionally remains. Never
blindly rerun an upload: Maven coordinates are immutable and a lost response does
not prove publication failed.

- **Central has published the pending version:** run the workflow again with
  `resume_version` set to that version. It checks out the pending source commit,
  skips uploading, confirms its public POM/AAR, and retries docs/tag finalization.
  Check the original deployment/source association in Portal before recovery;
  availability alone cannot prove who uploaded an artifact.
- **Central is still validating/publishing:** wait or resolve the deployment in
  Portal, then use recovery. Do not delete the pending tag while a deployment can
  still become public.
- **Central definitively rejected the attempt or no upload occurred:** drop any
  remaining deployment and verify no publication is possible. A maintainer can
  then delete exactly the failed attempt reference with
  `git push origin :refs/tags/release-pending/X.Y.Z` and start a fresh workflow.
  This is deliberately a manual decision, not automatic cleanup.
- **Git updates rejected:** fix the permission/rule problem, then recover without
  uploading again. If installation inputs changed on main, reconcile them with
  the published source before recovery; the workflow will not overwrite them.
- **Existing release tag or Central ahead of tags:** investigate the previous
  run/deployment. Never move an existing release tag or assign old artifacts to
  an unverified source commit.

Successful runs tag the artifact source, not the later documentation commit.
Do not treat a Git tag or a staged deployment alone as proof of publication.
