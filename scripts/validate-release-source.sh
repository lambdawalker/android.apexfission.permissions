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
cd "$validation_checkout/sites"
npm ci
npm run check
npm run screenshots:render
npm run screenshots:check
