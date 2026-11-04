package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.util.TraceIdFilter;
import com.csp.platform.dto.AuditLogQuery;
import com.csp.platform.entity.AuditLog;
import com.csp.platform.mapper.AuditLogMapper;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 审计服务：只追加，不提供更新/删除入口。
 * 记录失败不影响主业务（吞掉异常并告警），但调用方需保持幂等语义。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    /** 记录一条审计日志，自动从上下文补齐用户/traceId。 */
    public void record(String action, String resource, String detail, String result, Long costMs) {
        AuditLog a = new AuditLog();
        a.setTraceId(MDC.get(TraceIdFilter.TRACE_ID));
        LoginUser u = UserContext.get();
        if (u != null) {
            a.setUserId(u.getUserId());
            a.setUsername(u.getUsername());
            if (u.getClientIp() != null) a.setIp(u.getClientIp());
        }
        a.setAction(action);
        a.setResource(resource);
        a.setDetail(detail);
        a.setResult(result == null ? "SUCCESS" : result);
        a.setCostMs(costMs);
        a.setCreatedAt(LocalDateTime.now());
        try {
            auditLogMapper.insert(a);
        } catch (Exception e) {
            log.error("审计日志写入失败 action={} resource={}", action, resource, e);
        }
    }

    public void success(String action, String resource, String detail) {
        record(action, resource, detail, "SUCCESS", null);
    }

    public void fail(String action, String resource, String detail, Long costMs) {
        record(action, resource, detail, "FAIL", costMs);
    }

    public PageResult<AuditLog> page(AuditLogQuery q) {
        LambdaQueryWrapper<AuditLog> w = new LambdaQueryWrapper<>();
        if (q.getUserId() != null) w.eq(AuditLog::getUserId, q.getUserId());
        if (StringUtils.hasText(q.getUsername())) w.eq(AuditLog::getUsername, q.getUsername());
        if (StringUtils.hasText(q.getAction())) w.eq(AuditLog::getAction, q.getAction());
        if (StringUtils.hasText(q.getResult())) w.eq(AuditLog::getResult, q.getResult());
        if (StringUtils.hasText(q.getTraceId())) w.eq(AuditLog::getTraceId, q.getTraceId());
        if (q.getFrom() != null) w.ge(AuditLog::getCreatedAt, q.getFrom());
        if (q.getTo() != null) w.le(AuditLog::getCreatedAt, q.getTo());
        w.orderByDesc(AuditLog::getCreatedAt);
        Page<AuditLog> p = auditLogMapper.selectPage(new Page<>(q.getPage(), q.getSize()), w);
        return PageResult.of(p);
    }

    public AuditLog get(Long id) {
        AuditLog a = auditLogMapper.selectById(id);
        if (a == null) throw new com.csp.common.exception.BizException(
                com.csp.common.api.ResultCode.NOT_FOUND, "审计日志不存在：" + id);
        return a;
    }
}
