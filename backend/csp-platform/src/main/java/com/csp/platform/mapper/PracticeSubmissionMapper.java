package com.csp.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.platform.entity.PracticeSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** 练习提交 Mapper。 */
@Mapper
public interface PracticeSubmissionMapper extends BaseMapper<PracticeSubmission> {

    /** 练习排行榜：按累计得分排序，只统计已解出的题。 */
    @Select("SELECT ps.user_id AS userId, " +
            "       COALESCE(NULLIF(u.nickname, ''), u.username) AS name, " +
            "       u.username AS username, " +
            "       COUNT(DISTINCT ps.question_id) AS solved, " +
            "       COALESCE(SUM(ps.score), 0) AS score " +
            "FROM practice_submission ps " +
            "LEFT JOIN sys_user u ON u.id = ps.user_id " +
            "WHERE ps.correct = 1 AND ps.deleted = 0 " +
            "GROUP BY ps.user_id, u.username, u.nickname " +
            "ORDER BY score DESC, solved DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectLeaderboard(@Param("limit") int limit);
}
