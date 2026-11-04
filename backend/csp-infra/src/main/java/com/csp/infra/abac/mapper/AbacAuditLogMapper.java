package com.csp.infra.abac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.infra.abac.entity.AbacAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * ABAC 审计日志 Mapper。
 */
@Mapper
public interface AbacAuditLogMapper extends BaseMapper<AbacAuditLog> {
}
