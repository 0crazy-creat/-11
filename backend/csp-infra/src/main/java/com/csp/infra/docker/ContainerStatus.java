package com.csp.infra.docker;

/** 容器生命周期状态（对齐 GZCTF ContainerStatus）。 */
public enum ContainerStatus {
    PENDING,
    RUNNING,
    STOPPED,
    DESTROYED,
    ERROR
}
