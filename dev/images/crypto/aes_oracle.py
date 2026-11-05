#!/usr/bin/env python3
"""AES-CBC 字节翻转靶场。

提供三个接口：
  GET /info                      -> 返回 iv 与 secret 的密文（hex）
  GET /encrypt?data=<hex>        -> AES-128-CBC 加密，返回 hex
  GET /decrypt?data=<hex>        -> 解密并返回 hex（padding 非法时返回 padding error）
  GET /flag?data=<hex>           -> 解密结果含 admin=true 时返回 flag

secret 明文为 "admin=false;"，选手需要翻转前一块密文改变明文得到 admin=true。
"""
import binascii
import http.server
import os
import socketserver
import subprocess
import urllib.parse

KEY = "00112233445566778899aabbccddeeff"
IV = "000102030405060708090a0b0c0d0e0f"
SECRET = b"admin=false;"
FLAG = os.environ.get("FLAG", "flag{aes_cbc_bitflip_demo}")


def _pad(data: bytes) -> bytes:
    n = 16 - len(data) % 16
    return data + bytes([n]) * n


def _unpad(data: bytes) -> bytes:
    if not data:
        return data
    n = data[-1]
    if n < 1 or n > 16 or data[-n:] != bytes([n]) * n:
        raise ValueError("bad padding")
    return data[:-n]


def _aes(data: bytes, decrypt: bool = False) -> bytes:
    cmd = ["openssl", "enc", "-aes-128-cbc", "-K", KEY, "-iv", IV, "-nopad"]
    if decrypt:
        cmd.append("-d")
    proc = subprocess.run(cmd, input=data, capture_output=True)
    return proc.stdout


def _encrypt(plain: bytes) -> bytes:
    return _aes(_pad(plain))


def _decrypt(cipher: bytes) -> bytes:
    return _unpad(_aes(cipher, decrypt=True))


class Handler(http.server.BaseHTTPRequestHandler):
    server_version = "CryptoAesLab/1.0"

    def _reply(self, code: int, body: bytes) -> None:
        self.send_response(code)
        self.send_header("Content-Type", "text/plain; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):  # noqa: N802
        parsed = urllib.parse.urlparse(self.path)
        query = urllib.parse.parse_qs(parsed.query)
        raw = query.get("data", [""])[0]

        if parsed.path == "/":
            body = (
                "AES-128-CBC Oracle\n"
                "/info\n"
                "/encrypt?data=<hex>\n"
                "/decrypt?data=<hex>\n"
                "/flag?data=<hex>\n"
            )
            return self._reply(200, body.encode())

        if parsed.path == "/info":
            secret_ct = _encrypt(SECRET)
            body = f"iv={IV}\nsecret={binascii.hexlify(secret_ct).decode()}\n"
            return self._reply(200, body.encode())

        if parsed.path in ("/encrypt", "/decrypt", "/flag"):
            try:
                data = binascii.unhexlify(raw)
            except (binascii.Error, ValueError):
                return self._reply(400, b"bad hex")
            if not data or len(data) % 16 != 0:
                return self._reply(400, b"length must be multiple of 16")

            if parsed.path == "/encrypt":
                return self._reply(200, binascii.hexlify(_encrypt(data)).decode())

            try:
                plain = _decrypt(data)
            except ValueError:
                return self._reply(200, b"padding error")

            if parsed.path == "/decrypt":
                return self._reply(200, binascii.hexlify(plain).decode())

            if b"admin=true" in plain:
                return self._reply(200, FLAG.encode())
            return self._reply(200, b"not admin")

        return self._reply(404, b"not found")

    def log_message(self, *args):
        pass


class Server(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


if __name__ == "__main__":
    with Server(("0.0.0.0", 8080), Handler) as httpd:
        httpd.serve_forever()
