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
 * 场景实操赛多机靶场编排实例。
 *
 * <p>一个场景 = 一个 Docker 自定义 bridge 网络 + 多个容器（外网 Web / 内网 DB / 跳板机），
 * 内网容器之间通过服务名互通，只有 {@code entryNode} 映射宿主端口对外暴露。</p>
 */
@Data
@TableName("scenario_env")
public class ScenarioEnv implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;
    private Long teamId;
    private Long scenarioId;

    /** Docker 自定义网络名（内网互通） */
    private String networkName;
    /** compose 编排模板（YAML/JSON） */
    private String composeTpl;
    /** 对外暴露的入口节点名 */
    private String entryNode;
    /** 入口访问 host */
    private String entryHost;
    /** 入口映射到宿主机的端口 */
    private Integer entryPort;
    /** CREATING / RUNNING / FAILED / DESTROYED */
    private String status;
    /** 已拉起容器数 */
    private Integer nodeCount;
    /** 节点明细（JSON：name/image/ip/internal） */
    private String detail;
    /** 失败原因 */
    private String failReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer deleted;
}