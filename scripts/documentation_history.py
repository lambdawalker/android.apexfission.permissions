#!/usr/bin/env python3
"""Compatibility entry point for the Astro exporter; implementation lives in the pinned CLI."""
from pathlib import Path
import subprocess
import sys

if __name__ == "__main__":
    raise SystemExit(subprocess.call(
        ["android-repo-setup", "--project", str(Path(__file__).resolve().parents[1]),
         "documentation_history", *sys.argv[1:]]
    ))
