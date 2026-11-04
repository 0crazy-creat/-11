package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    /** 统计某用户在时间窗内的错误提交数（反作弊：暴力提交检测）。 */
    @Select("SELECT COUNT(1) FROM submission WHERE contest_id = #{contestId} AND user_id = #{userId} "
            + "AND correct = 0 AND created_at >= #{from}")
    int countWrongSince(@Param("contestId") Long contestId, @Param("userId") Long userId,
                        @Param("from") LocalDateTime from);

    /** 统计某 IP 在时间窗内正确提交涉及的用户数（反作弊：多账号同 IP 检测）。 */
    @Select("SELECT COUNT(DISTINCT user_id) FROM submission WHERE contest_id = #{contestId} "
            + "AND ip = #{ip} AND correct = 1 AND created_at >= #{from}")
    int countDistinctUsersByIp(@Param("contestId") Long contestId, @Param("ip") String ip,
                              @Param("from") LocalDateTime from);
}
