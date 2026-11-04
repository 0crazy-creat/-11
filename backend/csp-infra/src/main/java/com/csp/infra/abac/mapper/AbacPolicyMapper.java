package com.csp.infra.abac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.infra.abac.entity.AbacPolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * ABAC 策略 Mapper。
 */
@Mapper
public interface AbacPolicyMapper extends BaseMapper<AbacPolicy> {
}
