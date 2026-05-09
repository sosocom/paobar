#!/usr/bin/env bash
# 离线开发辅助脚本：把当前 Flutter SDK 内置的 CanvasKit 拷贝到 `web/canvaskit/`，
# 以便 `flutter run -d chrome` 时不再从 gstatic.com 下载，解决内网 / 代理环境下
# 首屏白屏 + TypeError: Failed to fetch canvaskit.js 的问题。
#
# 用法：
#   bash scripts/prepare_web_canvaskit.sh
#   然后正常 `flutter run -d chrome ...`，index.html 里已声明 canvasKitBaseUrl=canvaskit/
set -euo pipefail
cd "$(dirname "$0")/.."

SRC="$(flutter --machine --version 2>/dev/null | awk -F'"' '/flutterRoot/{print $4}')"
if [[ -z "${SRC:-}" ]]; then
  SRC="${FLUTTER_ROOT:-}"
fi
if [[ -z "${SRC:-}" || ! -d "${SRC}/bin/cache/flutter_web_sdk/canvaskit" ]]; then
  echo "找不到 Flutter SDK 的 canvaskit 目录。请先设置 FLUTTER_ROOT 环境变量或把 flutter 放到 PATH。" >&2
  exit 1
fi

DST="web/canvaskit"
mkdir -p "$DST"
cp -R "${SRC}/bin/cache/flutter_web_sdk/canvaskit/"* "$DST/"
echo "✅ CanvasKit 已拷贝到 $DST ($(du -sh "$DST" | awk '{print $1}'))"
