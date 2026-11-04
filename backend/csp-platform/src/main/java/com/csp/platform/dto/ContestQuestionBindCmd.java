package com.csp.platform.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 赛事组卷入参。 */
@Data
public class ContestQuestionBindCmd {
    @NotEmpty(message = "题目列表不能为空")
    private List<@NotNull Long> questionIds;
    /** 是否覆盖已有组卷；false 表示追加 */
    private boolean overwrite = true;
}
