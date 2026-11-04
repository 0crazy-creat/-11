package com.csp.infra.docker;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 靶机创建规格。 */
@Data
@Builder
public class TargetSpec {
    /** 竞赛ID */
    private Long contestId;
    /** 战队ID */
    private Long teamId;
    /** 题目ID */
    private Long questionId;
    /** 镜像名 */
    private String image;
    /** 容器内暴露端口 */
    private int containerPort;
    /** 环境变量（注入 flag 等） */
    private Map<String, String> env;
    /** CPU 限额，如 "1.0" */
    private String cpuLimit;
    /** 内存限额，如 "512m" */
    private String memoryLimit;
    /** 是否只允许内网访问 */
    private boolean internalOnly;
    /** 附加命令 */
    private List<String> cmd;

    public String containerName() {
        return "csp-c" + contestId + "-t" + teamId + "-q" + questionId;
    }
}
