package com.csp.arena.config;

import lombok.Data;

import java.io.Serializable;

/** 统一响应体。 */
@Data
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();
    private boolean success;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0; r.message = "操作成功"; r.data = data; r.success = true;
        return r;
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code; r.message = message; r.success = false;
        return r;
    }
}
