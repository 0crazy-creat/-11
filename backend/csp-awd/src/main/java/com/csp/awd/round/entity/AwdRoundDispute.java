package com.csp.awd.round.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AWD 轮次申诉仲裁记录。
 *
 * <p>流程：选手提交申诉（PENDING）→ JUDGE 仲裁 approve / reject。
 * approve 时回滚该队本轮扣分，并把实际回滚分值写入 {@link #rollbackScore}。</p>
 */
@Data
@TableName("awd_round_dispute")
public class AwdRoundDispute {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 竞赛ID */
    private Long contestId;
    /** 轮次号 */
    private Integer roundNo;
    /** 申诉队伍 */
    private Long teamId;
    /** 关联题目/靶机，null 表示针对整轮 */
    private Long questionId;
    /** 申诉类型：SCORE / SLA / FLAG / OTHER */
    private String disputeType;
    /** 申诉理由（选手填写） */
    private String reason;
    /** 证据链接/说明 */
    private String evidence;
    /** PENDING / APPROVED / REJECTED */
    private String status;
    /** 申请人用户ID */
    private Long applicantId;
    /** 仲裁人用户ID */
    private Long arbiterId;
    /** 仲裁意见 */
    private String arbitrateNote;
    /** 仲裁时间 */
    private LocalDateTime arbitratedAt;
    /** 实际回滚分值（approve 时写入） */
    private BigDecimal rollbackScore;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0否 1是 */
    private Integer deleted;
}