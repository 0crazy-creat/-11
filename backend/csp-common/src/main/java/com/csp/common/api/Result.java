package com.csp.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 统一响应体。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();
    private String traceId;

    public static <T> Result<T> ok() { return ok(null); }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(T data, String message) {
        Result<T> r = ok(data);
        r.message = message;
        return r;
    }

    public static <T> Result<T> fail(ResultCode rc) { return fail(rc.getCode(), rc.getMessage()); }

    public static <T> Result<T> fail(ResultCode rc, String message) { return fail(rc.getCode(), message); }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return code == ResultCode.SUCCESS.getCode(); }
}
