package com.csp.awd.round.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.awd.round.entity.AwdRoundDispute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** AWD 轮次申诉 Mapper。 */
@Mapper
public interface AwdRoundDisputeMapper extends BaseMapper<AwdRoundDispute> {

    /** 从赛事战队成员关系解析用户所属战队（一人多队取最早加入的一支）。 */
    @Select("SELECT team_id FROM contest_team_member "
            + "WHERE contest_id = #{contestId} AND user_id = #{userId} AND deleted = 0 "
            + "ORDER BY joined_at ASC, id ASC LIMIT 1")
    List<Map<String, Object>> selectTeamIdByUser(@Param("contestId") Long contestId,
                                                 @Param("userId") Long userId);
}
