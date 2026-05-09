#!/usr/bin/env python3
"""
SPA 静态服务器：把 Flutter `build/web` 服务起来，并在路径不存在时
回落到 `index.html`（让 `/index`、`/now-playing/xxx` 这种前端路由刷新后也能工作）。

Usage:
    python3 scripts/serve_web.py [port]
    # 或
    python3 scripts/serve_web.py 8787
"""
from __future__ import annotations

import os
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer


ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "build", "web")


class SpaFallbackHandler(SimpleHTTPRequestHandler):
    def translate_path(self, path: str) -> str:  # noqa: D401
        """真实文件存在就正常返回；否则回落到 index.html。"""
        fs_path = super().translate_path(path)
        if os.path.isfile(fs_path):
            return fs_path
        if os.path.isdir(fs_path):
            idx = os.path.join(fs_path, "index.html")
            if os.path.isfile(idx):
                return idx
        return os.path.join(ROOT, "index.html")

    def end_headers(self) -> None:
        # 开发期禁缓存，避免改了还一直看老页面
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()


def main() -> None:
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8787
    if not os.path.isdir(ROOT):
        print(f"[!] {ROOT} 不存在，先跑 `flutter build web`。", file=sys.stderr)
        sys.exit(1)

    os.chdir(ROOT)
    httpd = ThreadingHTTPServer(("", port), SpaFallbackHandler)
    print(f"SPA server: http://localhost:{port}/  (serving {ROOT})")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()


if __name__ == "__main__":
    main()
