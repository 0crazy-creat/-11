package com.csp.common.exception;

import com.csp.common.api.ResultCode;
import lombok.Getter;

/** 业务异常：由全局异常处理器统一转换为标准响应。 */
@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BizException(ResultCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
    }

    public BizException(ResultCode rc, String message) {
        super(message);
        this.code = rc.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BizException of(String message) { return new BizException(message); }

    public static BizException notFound(String what) { return new BizException(ResultCode.NOT_FOUND, what + "不存在"); }

    public static BizException forbidden(String message) { return new BizException(ResultCode.FORBIDDEN, message); }

    public static BizException unauthorized(String message) { return new BizException(ResultCode.UNAUTHORIZED, message); }
}
