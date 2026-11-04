package com.csp.ctf.judge;

import com.csp.common.util.FlagUtils;
import org.springframework.stereotype.Component;

/**
 * 静态 flag 判题：题库 flag 字段常量时间比对。
 * <p>兼容历史数据：部分题目答案写在 answer 字段（题库复用）。</p>
 */
@Component
public class StaticFlagValidator implements FlagValidator {

    @Override
    public JudgeMode mode() {
        return JudgeMode.STATIC;
    }

    @Override
    public boolean validate(JudgeContext ctx) {
        String submitted = FlagUtils.normalize(ctx.getSubmitted());
        if (submitted.isEmpty() || ctx.getQuestion() == null) return false;

        String flag = ctx.getQuestion().getFlag();
        if (flag != null && !flag.isBlank()
                && FlagUtils.safeEquals(FlagUtils.normalize(flag), submitted)) {
            return true;
        }
        String answer = ctx.getQuestion().getAnswer();
        return answer != null && !answer.isBlank()
                && FlagUtils.safeEquals(FlagUtils.normalize(answer), submitted);
    }
}