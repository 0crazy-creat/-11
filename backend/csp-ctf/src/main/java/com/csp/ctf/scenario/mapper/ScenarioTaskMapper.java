package com.csp.ctf.scenario.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.scenario.entity.ScenarioTask;
import org.apache.ibatis.annotations.Mapper;

/** 场景任务依赖 Mapper。 */
@Mapper
public interface ScenarioTaskMapper extends BaseMapper<ScenarioTask> {
}