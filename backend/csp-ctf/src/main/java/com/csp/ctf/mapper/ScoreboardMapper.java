package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.entity.Scoreboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScoreboardMapper extends BaseMapper<Scoreboard> {

    /** 按分数降序、lastSolveAt 升序取排行榜。 */
    @Select("SELECT * FROM scoreboard WHERE contest_id = #{contestId} "
            + "ORDER BY score DESC, last_solve_at ASC, id ASC LIMIT #{limit} OFFSET #{offset}")
    List<Scoreboard> selectRank(@Param("contestId") Long contestId, @Param("offset") long offset,
                                @Param("limit") long limit);

    @Select("SELECT COUNT(1) FROM scoreboard WHERE contest_id = #{contestId}")
    long countByContest(@Param("contestId") Long contestId);

    /**
     * 查询该赛事「每题最早正确提交」的战队集合，用于首杀标识。
     * 无首杀字段，按 contest+question 分组取 MIN(created_at) 对应行推导。
     */
    @Select("SELECT s.team_id FROM submission s JOIN ("
            + "  SELECT question_id, MIN(created_at) AS ft FROM submission"
            + "  WHERE contest_id = #{contestId} AND correct = 1 AND deleted = 0"
            + "  GROUP BY question_id"
            + ") f ON s.question_id = f.question_id AND s.created_at = f.ft"
            + " WHERE s.contest_id = #{contestId} AND s.correct = 1 AND s.deleted = 0")
    java.util.List<Long> selectFirstBloodTeams(@Param("contestId") Long contestId);
}
