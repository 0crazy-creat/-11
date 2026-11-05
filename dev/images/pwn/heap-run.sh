#!/bin/sh
exec socat TCP-LISTEN:9999,reuseaddr,fork EXEC:/pwn/heap,stderr
