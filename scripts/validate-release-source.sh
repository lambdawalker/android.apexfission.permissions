#!/usr/bin/env bash
set -euo pipefail
release_checkout="$PWD"
validation_root=$(mktemp -d "$RUNNER_TEMP/release-docs.XXXXXX")
validation_checkout="$validation_root/source"
cleanup() {
  git -C "$release_checkout" worktree remove --force "$validation_checkout" || true
  rmdir "$validation_root" || true
}
trap cleanup EXIT
# Rendering regenerates tracked reference files; isolate it from artifact provenance checks.
git worktree add --detach "$validation_checkout" "$SOURCE_SHA"
cd "$validation_checkout"
node sites/scripts/sync-examples.mjs
node sites/scripts/screenshots.mjs manifest
android-repo-setup build_docs --check
npm --prefix sites ci
npm --prefix sites run screenshots:render
npm --prefix sites run screenshots:check
