package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.entity.ContestTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ContestTeamMapper extends BaseMapper<ContestTeam> {

    /** 战队当前成员数（注册时校验上限）。 */
    @Select("SELECT COUNT(1) FROM contest_team_member WHERE team_id = #{teamId}")
    int countMembers(@Param("teamId") Long teamId);
}
