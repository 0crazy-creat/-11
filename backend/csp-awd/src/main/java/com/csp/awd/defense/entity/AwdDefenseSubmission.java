package com.csp.awd.defense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AWDP 防御修补提交记录。
 *
 * <p>与攻击 flag 提交（submission 表）<b>完全分离</b>：AWDP 的核心差异就是
 * 「修补包上传 → 平台侧真实验证 → 防御分结算」这条闭环，独立成表便于审计与复盘。</p>
 */
@Data
@TableName("awd_defense_submission")
public class AwdDefenseSubmission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 赛事ID（contest.type 必须为 AWDP） */
    private Long contestId;
    /** 轮次ID（awd_round.id） */
    private Long roundId;
    /** 轮次号（幂等五元组组成部分） */
    private Integer roundNo;
    /** 提交战队 */
    private Long teamId;
    /** 题目/靶机 */
    private Long questionId;
    /** 提交人 */
    private Long userId;

    /** 原始文件名 */
    private String fileName;
    /** 包大小（字节） */
    private Long fileSize;
    /** 包 SHA-256（幂等键 + 防篡改） */
    private String fileSha256;
    /** 服务端落盘路径 */
    private String storePath;
    /** ZIP / TAR / PATCH */
    private String packageType;

    /** PENDING/CHECKING/APPLYING/VERIFIED/REJECTED/FAILED/COMPROMISED */
    private String status;
    /** 本次使用的 check 脚本名 */
    private String checkScript;
    /** check 输出（截断） */
    private String checkOutput;
    /** 应用输出（截断） */
    private String applyOutput;
    /** 应用后重启是否成功 */
    private Integer restartOk;
    /** 修补后健康检查是否通过 */
    private Integer healthOk;
    /** 修补后仍被攻破（1=本轮防御分归零） */
    private Integer compromised;
    /** 本次结算的防御分增量 */
    private Integer defenseScore;
    /** 验证失败原因（返回给战队） */
    private String failReason;
    /** 战队修补说明 */
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}