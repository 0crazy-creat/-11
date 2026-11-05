#!/usr/bin/env python3
"""内存取证靶场：首页给出 memory.dump 下载，dump 中埋有 flag。"""
import os
from http.server import HTTPServer, SimpleHTTPRequestHandler


def read_flag() -> bytes:
    for path in ("/flag.txt", "/app/flag.txt"):
        if os.path.exists(path):
            with open(path, "rb") as fh:
                return fh.read().strip()
    return b"flag{forensics_demo}"


class Handler(SimpleHTTPRequestHandler):
    def do_GET(self):  # noqa: N802
        if self.path == "/":
            self.send_response(200)
            self.send_header("Content-type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write("<h1>内存取证挑战</h1>".encode())
            self.wfile.write(b"<p>download: <a href=/memory.dump>memory.dump</a></p>")
            self.wfile.write("<p>use Volatility or strings</p>".encode())
        elif self.path == "/memory.dump":
            self.send_response(200)
            self.send_header("Content-type", "application/octet-stream")
            self.end_headers()
            noise = b"\x00" * 1000 + b"SECRET_DATA_" + read_flag() + b"_END_SECRET"
            self.wfile.write(noise)
        else:
            super().do_GET()

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    print("forensics service on port 8080")
    HTTPServer(("0.0.0.0", 8080), Handler).serve_forever()
