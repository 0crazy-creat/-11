package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    /** 真实战队名唯一性需跨越逻辑删除行，避免与已解散但保留历史数据的战队冲突。 */
    @Select("SELECT COUNT(1) FROM team WHERE name = #{name} AND deleted = 0")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(1) FROM team WHERE invite_code = #{code}")
    int countByInviteCode(@Param("code") String code);

    /** 该战队当前生效的参赛记录数。 */
    @Select("SELECT COUNT(1) FROM contest_team WHERE team_id = #{teamId} AND status <> 'QUIT' AND deleted = 0")
    int countEnrollments(@Param("teamId") Long teamId);

    @Update("UPDATE team SET invite_code = NULL WHERE id = #{teamId}")
    void clearInviteCode(@Param("teamId") Long teamId);
}
