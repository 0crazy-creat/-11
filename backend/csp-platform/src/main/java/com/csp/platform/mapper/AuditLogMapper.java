package com.csp.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.platform.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/** 审计日志 Mapper。 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
