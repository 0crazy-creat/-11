package com.csp.ctf.scenario.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 场景实操赛任务依赖关系（Scenario → Task 树）。
 *
 * <p>题目主体复用 {@code question} 表（bank_type='SCENARIO'），本表是「依赖链」的强类型物化，
 * 用于环检测、拓扑排序与多机编排关联。依赖真值同时写在 {@code question.ext.preQuestionId}，
 * 本表与之保持同步（由 {@link com.csp.ctf.scenario.service.ScenarioDependencyGuard} 维护）。</p>
 */
@Data
@TableName("scenario_task")
public class ScenarioTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 竞赛ID */
    private Long contestId;
    /** 场景题ID（question.id） */
    private Long scenarioId;
    /** 任务题ID（question.id） */
    private Long taskId;
    /** 前置任务ID，NULL 表示无依赖 */
    private Long preTaskId;
    /** 同场景内排序 */
    private Integer orderNo;
    /** 任务标题快照 */
    private String title;
    /** 任务分值快照 */
    private Integer score;
    /** 解锁方式：SEQUENTIAL（顺序）/ ANY（任意） */
    private String unlockMode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0 否 1 是 */
    private Integer deleted;
}