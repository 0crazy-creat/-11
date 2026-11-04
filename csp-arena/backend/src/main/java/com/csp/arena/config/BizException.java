package com.csp.arena.config;

/** 业务异常。 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(400, message);
    }

    public static BizException of(String msg) { return new BizException(400, msg); }
    public static BizException notFound(String what) { return new BizException(404, what + "不存在"); }

    public int getCode() { return code; }
}
