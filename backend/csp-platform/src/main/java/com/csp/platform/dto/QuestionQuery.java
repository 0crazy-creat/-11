package com.csp.platform.dto;

import com.csp.common.api.PageQuery;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 题库分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionQuery extends PageQuery {
    /** THEORY/CTF/SCENARIO/AWD */
    @Pattern(regexp = "^(THEORY|CTF|SCENARIO|AWD)?$", message = "bankType 非法")
    private String bankType;
    private String category;
    private String difficulty;
    /** 标题/题面关键字 */
    private String keyword;
    private Integer status;
}
