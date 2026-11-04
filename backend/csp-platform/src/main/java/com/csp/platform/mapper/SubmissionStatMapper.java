package com.csp.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 归档所需的跨表统计（只读聚合查询）。 */
@Mapper
public interface SubmissionStatMapper {

    @Select("SELECT COUNT(*) FROM submission WHERE contest_id = #{contestId}")
    long countSubmissions(@Param("contestId") Long contestId);

    @Select("SELECT COUNT(DISTINCT user_id) FROM contest_team_member WHERE contest_id = #{contestId}")
    long countPlayers(@Param("contestId") Long contestId);
}
