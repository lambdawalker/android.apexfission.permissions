#!/usr/bin/env bash
set -euo pipefail
python3 scripts/module_release.py finalize --module permission --version "${RELEASE_VERSION:?}" --source "${SOURCE_SHA:?}"
