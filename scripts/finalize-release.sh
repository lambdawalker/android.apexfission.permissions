#!/usr/bin/env bash
# Called only after public Central confirmation and deterministic docs validation.
set -euo pipefail
: "${RELEASE_VERSION:?}" "${SOURCE_SHA:?}"
generated=$(mktemp)
trap 'rm -f "$generated"' EXIT
cp IMPORT.md "$generated"
git restore IMPORT.md
git fetch origin main --tags
git merge-base --is-ancestor "$SOURCE_SHA" origin/main
# Do not overwrite installation changes made while publication was in flight.
git diff --exit-code "$SOURCE_SHA" origin/main -- IMPORT.md docs/templates/IMPORT.md.template gradle.properties permission/build.gradle.kts scripts/release.py build.gradle.kts
git switch -C release-docs origin/main
cp "$generated" IMPORT.md
git config user.name 'github-actions[bot]'
git config user.email '41898282+github-actions[bot]@users.noreply.github.com'
git add IMPORT.md
if ! git diff --cached --quiet; then
  git commit -m "docs: update installation to $RELEASE_VERSION"
fi
git tag -a "v$RELEASE_VERSION" "$SOURCE_SHA" -m "Maven Central release $RELEASE_VERSION"
# Either all three updates land or none do. Never force-update main or release tags.
git push --atomic origin HEAD:refs/heads/main "refs/tags/v$RELEASE_VERSION" ":refs/tags/release-pending/$RELEASE_VERSION"
