package com.csp.ctf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 答题/提交 flag 请求。 */
@Data
public class SubmitRequest {
    @NotNull(message = "赛事ID不能为空")
    private Long contestId;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotBlank(message = "提交内容不能为空")
    @Size(max = 512, message = "提交内容过长")
    private String content;

    /** FLAG（CTF题）/ ANSWER（理论题），默认 FLAG */
    private String submitType = "FLAG";

    /** 前端记录的答题开始时间戳（毫秒），用于计算耗时与反作弊 */
    private Long startTs;

    private String deviceId;
}
