package com.csp.infra.docker;

/** 容器网络模式（对齐 GZCTF NetworkMode）。 */
public enum NetworkMode {
    /** 可访问外网 */
    OPEN,
    /** 禁止出站（默认） */
    ISOLATED,
    /** 自定义网络 */
    CUSTOM
}
