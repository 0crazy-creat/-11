package com.csp.ctf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 判分结果。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResult {
    /** 是否正确 */
    private boolean correct;
    /** 本次得分（正确时为首杀/普通得分，错误为 0） */
    private int score;
    /** 是否首杀 */
    private boolean firstBlood;
    /** 提示信息 */
    private String message;
    /** 累计总分 */
    private long totalScore;
    /** 已解出题数 */
    private int solved;
    /** 耗时毫秒 */
    private Long elapsedMs;
}
