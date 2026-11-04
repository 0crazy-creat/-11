package com.csp.arena.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 全局异常处理：统一返回 Result，避免前端出现裸 400/500。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> biz(BizException e) {
        log.warn("[业务异常] {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> missingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少必要参数：" + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getField() + " " + f.getDefaultMessage()).orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> noResource(NoResourceFoundException e) {
        log.warn("[资源不存在] {}", e.getResourcePath());
        return Result.fail(404, "接口不存在：" + e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> other(Exception e) {
        log.error("[未捕获异常]", e);
        return Result.fail(500, "服务异常：" + e.getMessage());
    }
}
