#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

: "${API_BASE_URL:=https://api.paobar.example.com}"
: "${FLAVOR:=prod}"

flutter build ios --release --no-codesign \
  --dart-define=API_BASE_URL="$API_BASE_URL" \
  --dart-define=FLAVOR="$FLAVOR"
