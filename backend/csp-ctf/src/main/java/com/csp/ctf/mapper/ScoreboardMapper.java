package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.dto.ChallengeMeta;
import com.csp.ctf.dto.FirstBloodRow;
import com.csp.ctf.dto.SolveCell;
import com.csp.ctf.dto.StageBoardRow;
import com.csp.ctf.entity.Scoreboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
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
     * 某队在本赛事已解出的题目数（去重），用于实时判分时维护 solved 字段。
     */
    @Select("SELECT COUNT(DISTINCT question_id) FROM submission"
            + " WHERE contest_id = #{contestId} AND team_id = #{teamId} AND correct = 1 AND deleted = 0")
    int countSolvedQuestions(@Param("contestId") Long contestId, @Param("teamId") Long teamId);

    /**
     * 首杀明细（全量）：每题最早的一条正确提交。
     * 同一题多人同秒提交时取 id 最小者，保证展示稳定。
     */
    @Select("SELECT x.question_id, x.team_id, x.user_id, x.created_at AS solved_at FROM ("
            + "  SELECT s.question_id, s.team_id, s.user_id, s.created_at,"
            + "         ROW_NUMBER() OVER (PARTITION BY s.question_id"
            + "           ORDER BY s.created_at ASC, s.id ASC) rn"
            + "  FROM submission s"
            + "  WHERE s.contest_id = #{contestId} AND s.correct = 1 AND s.deleted = 0"
            + ") x WHERE x.rn = 1 ORDER BY x.created_at ASC")
    List<FirstBloodRow> selectFirstBloodDetail(@Param("contestId") Long contestId);

    /** 首杀明细（截至某时刻）：用于阶段榜的一血角标回放。 */
    @Select("SELECT x.question_id, x.team_id, x.user_id, x.created_at AS solved_at FROM ("
            + "  SELECT s.question_id, s.team_id, s.user_id, s.created_at,"
            + "         ROW_NUMBER() OVER (PARTITION BY s.question_id"
            + "           ORDER BY s.created_at ASC, s.id ASC) rn"
            + "  FROM submission s"
            + "  WHERE s.contest_id = #{contestId} AND s.correct = 1 AND s.deleted = 0"
            + "    AND s.created_at <= #{asOf}"
            + ") x WHERE x.rn = 1 ORDER BY x.created_at ASC")
    List<FirstBloodRow> selectFirstBloodDetailAsOf(@Param("contestId") Long contestId,
                                                   @Param("asOf") LocalDateTime asOf);

    /**
     * 阶段榜/历史回放：截至 asOf 的累计榜。
     * 每题每队只计最早一次正确提交的分数（与实时判分口径一致），
     * 分数取当时提交记录分值，因此回放结果不受后续动态衰减影响。
     */
    @Select("SELECT t.team_id, SUM(t.score) AS score, COUNT(*) AS solved, MAX(t.created_at) AS last_solve_at FROM ("
            + "  SELECT s.team_id, s.question_id, s.score, s.created_at,"
            + "         ROW_NUMBER() OVER (PARTITION BY s.team_id, s.question_id"
            + "           ORDER BY s.created_at ASC, s.id ASC) rn"
            + "  FROM submission s"
            + "  WHERE s.contest_id = #{contestId} AND s.correct = 1 AND s.deleted = 0"
            + "    AND s.created_at <= #{asOf}"
            + ") t WHERE t.rn = 1 GROUP BY t.team_id"
            + " ORDER BY score DESC, last_solve_at ASC, t.team_id ASC LIMIT #{limit}")
    List<StageBoardRow> selectBoardAsOf(@Param("contestId") Long contestId,
                                        @Param("asOf") LocalDateTime asOf,
                                        @Param("limit") long limit);

    /**
     * 解题矩阵（全量）：每队每题只取最早一次正确提交，并给出该题的解出顺序。
     * blood_rank = 1 即一血，2/3 为二三血，用于榜单单元格的特殊标识。
     */
    @Select("SELECT t.team_id, t.question_id, t.score, t.created_at AS solved_at,"
            + "       ROW_NUMBER() OVER (PARTITION BY t.question_id"
            + "         ORDER BY t.created_at ASC, t.team_id ASC) AS blood_rank"
            + " FROM (SELECT s.team_id, s.question_id, s.score, s.created_at,"
            + "              ROW_NUMBER() OVER (PARTITION BY s.team_id, s.question_id"
            + "                ORDER BY s.created_at ASC, s.id ASC) rn"
            + "       FROM submission s"
            + "       WHERE s.contest_id = #{contestId} AND s.correct = 1 AND s.deleted = 0) t"
            + " WHERE t.rn = 1 ORDER BY t.created_at ASC")
    List<SolveCell> selectSolveCells(@Param("contestId") Long contestId);

    /** 解题矩阵（截至某时刻）：阶段榜回放用。 */
    @Select("SELECT t.team_id, t.question_id, t.score, t.created_at AS solved_at,"
            + "       ROW_NUMBER() OVER (PARTITION BY t.question_id"
            + "         ORDER BY t.created_at ASC, t.team_id ASC) AS blood_rank"
            + " FROM (SELECT s.team_id, s.question_id, s.score, s.created_at,"
            + "              ROW_NUMBER() OVER (PARTITION BY s.team_id, s.question_id"
            + "                ORDER BY s.created_at ASC, s.id ASC) rn"
            + "       FROM submission s"
            + "       WHERE s.contest_id = #{contestId} AND s.correct = 1 AND s.deleted = 0"
            + "         AND s.created_at <= #{asOf}) t"
            + " WHERE t.rn = 1 ORDER BY t.created_at ASC")
    List<SolveCell> selectSolveCellsAsOf(@Param("contestId") Long contestId,
                                         @Param("asOf") LocalDateTime asOf);

    /** 赛事题目表头：分值优先取赛事配置，未配置（0/null）回退题库分值。 */
    @Select("SELECT q.id AS question_id, q.title, q.category, q.difficulty,"
            + "       IFNULL(NULLIF(cq.score, 0), q.score) AS score, cq.sort_no"
            + " FROM contest_question cq"
            + " JOIN question q ON q.id = cq.question_id"
            + " WHERE cq.contest_id = #{contestId} AND cq.deleted = 0 AND cq.visible = 1"
            + "   AND q.deleted = 0"
            + " ORDER BY cq.sort_no ASC, q.id ASC")
    List<ChallengeMeta> selectContestChallenges(@Param("contestId") Long contestId);

    /** 参赛队伍数（排行榜正确率分母）。 */
    @Select("SELECT COUNT(1) FROM contest_team WHERE contest_id = #{contestId}"
            + " AND deleted = 0 AND status <> 'QUIT'")
    int countContestTeams(@Param("contestId") Long contestId);
}
