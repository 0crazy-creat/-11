package com.csp.cfs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** CFS flag 提交入参。 */
@Data
public class CfsSubmitCmd {

    @NotNull(message = "赛事ID不能为空")
    private Long contestId;

    @NotNull(message = "节点ID不能为空")
    private Long nodeId;

    /** 提交的 flag */
    private String flag;

    /** 客户端开始时间戳（可选，用于耗时统计） */
    private Long startTs;

    private String deviceId;
}