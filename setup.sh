#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
reference=$(sed -n "s/^tooling_ref: ['\"]*\([^'\"]*\)['\"]*$/\1/p" .project/setup/config.yaml)
if [[ "$reference" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  ref=(--tag "$reference")
elif [[ "$reference" =~ ^[0-9a-f]{40}$ ]]; then
  ref=(--rev "$reference")
else
  echo 'Invalid tooling_ref in .project/setup/config.yaml' >&2
  exit 1
fi
cargo install --locked --git https://github.com/lambdawalker/gradle.android-repo-setup.git "${ref[@]}" android-repo-setup
exec android-repo-setup "$@"
