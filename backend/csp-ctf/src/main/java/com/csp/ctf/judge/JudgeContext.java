package com.csp.ctf.judge;

import com.csp.ctf.entity.CtfQuestion;
import lombok.Builder;
import lombok.Data;

/**
 * 判题上下文：一次提交判定所需的全部输入。
 * <p>动态 flag 依赖 contestId+teamId+questionId 三元组派生，因此必须显式传入。</p>
 */
@Data
@Builder
public class JudgeContext {
    private Long contestId;
    private Long teamId;
    private Long userId;
    private CtfQuestion question;
    /** 用户原始提交内容（未归一化） */
    private String submitted;
    /** 从 question.ext 解析出的扩展配置，可能为 null */
    private QuestionExt ext;
}