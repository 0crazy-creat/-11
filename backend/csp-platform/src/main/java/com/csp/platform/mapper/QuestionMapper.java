package com.csp.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.platform.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/** 题库 Mapper。 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
