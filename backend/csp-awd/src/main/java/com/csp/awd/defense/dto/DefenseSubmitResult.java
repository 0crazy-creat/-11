package com.csp.awd.defense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** AWDP 修补包提交结果（含平台侧真实验证的每一步证据）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSubmitResult {

    /** 提交记录ID */
    private Long submissionId;
    /** 是否验证通过并结算防御分 */
    private boolean verified;
    /** 状态：VERIFIED/REJECTED/FAILED/COMPROMISED/DUPLICATE */
    private String status;
    /** 面向战队的结论说明 */
    private String message;
    /** 本次结算的防御分增量 */
    private Integer gainedScore;
    /** 结算后队伍总分 */
    private Long totalScore;

    // ---- 验证过程证据（前端可逐步展示） ----
    /** 修补包 SHA-256 */
    private String sha256;
    /** 包大小（字节） */
    private Long fileSize;
    /** 包类型 ZIP/TAR/PATCH */
    private String packageType;
    /** 靶机容器名（用于 docker exec 复核） */
    private String containerName;
    /** 容器内修补包落地路径，如 /tmp/patch/xxx.zip */
    private String remotePath;
    /** 1) 拷入容器是否成功 */
    private Boolean copyOk;
    /** 2) 解包/应用是否成功 */
    private Boolean applyOk;
    /** 3) 重启靶机是否成功 */
    private Boolean restartOk;
    /** 4) 健康检查是否通过（修补后靶机仍在线） */
    private Boolean healthOk;
    /** 5) check 脚本是否通过（漏洞是否真被修掉） */
    private Boolean checkOk;
    /** check 脚本退出码 */
    private Integer checkExitCode;
    /** check 脚本输出（截断 2000 字） */
    private String checkOutput;
    /** 应用过程输出（截断 2000 字） */
    private String applyOutput;
    /** 失败原因明细（逐条，便于战队定位） */
    private List<String> failReasons;
    /** 人工复核用的 docker exec 命令 */
    private String verifyCommand;
}