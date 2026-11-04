package com.csp.awd.round.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AWD 轮次控制审计日志：begin / running / settle / 软中断 / 硬中断 / 申诉 / 仲裁 全量留痕。
 *
 * <p>csp-awd 不能反向依赖 csp-platform 的 AuditService，因此轮次控制面独立落库，
 * 保证「谁在什么时候对哪一轮做了什么」可追溯。</p>
 */
@Data
@TableName("awd_round_audit")
public class AwdRoundAudit {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;
    private Integer roundNo;
    /** BEGIN / RUNNING / SETTLE / INTERRUPT_SOFT / INTERRUPT_HARD / DISPUTE / ARBITRATE */
    private String action;
    /** 作用对象ID（申诉ID等） */
    private Long targetId;
    private Long operatorId;
    private String operator;
    private String detail;
    private LocalDateTime createTime;
}