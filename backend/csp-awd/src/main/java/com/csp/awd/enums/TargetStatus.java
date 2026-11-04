package com.csp.awd.enums;

/** 靶机生命周期：PENDING -> RUNNING -> STOPPED/ERROR。 */
public enum TargetStatus {
    PENDING, RUNNING, STOPPED, ERROR;

    public boolean isUsable() { return this == RUNNING; }
}
