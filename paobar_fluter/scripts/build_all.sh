#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

bash scripts/codegen.sh
bash scripts/l10n.sh
bash scripts/build_web.sh

if [[ "$(uname)" == "Darwin" ]]; then
  bash scripts/build_macos.sh
  bash scripts/build_ios.sh
fi

bash scripts/build_android.sh

if [[ "$OS" == "Windows_NT" ]] 2>/dev/null; then
  bash scripts/build_windows.sh
fi
