package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.entity.ChallengeContainer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChallengeContainerMapper extends BaseMapper<ChallengeContainer> {

    @Select("SELECT * FROM challenge_container WHERE expect_stop_at < #{now} AND status = 'RUNNING' ORDER BY expect_stop_at ASC")
    List<ChallengeContainer> selectDyingContainers(@Param("now") LocalDateTime now);

    @Select("SELECT * FROM challenge_container WHERE contest_id = #{contestId} AND team_id = #{teamId} AND status = 'RUNNING' ORDER BY created_at DESC")
    List<ChallengeContainer> selectTeamRunningContainers(@Param("contestId") Long contestId, @Param("teamId") Long teamId);
}
