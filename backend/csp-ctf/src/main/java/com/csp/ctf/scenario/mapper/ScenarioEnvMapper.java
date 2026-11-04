package com.csp.ctf.scenario.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.scenario.entity.ScenarioEnv;
import org.apache.ibatis.annotations.Mapper;

/** 场景多机靶场编排 Mapper。 */
@Mapper
public interface ScenarioEnvMapper extends BaseMapper<ScenarioEnv> {
}