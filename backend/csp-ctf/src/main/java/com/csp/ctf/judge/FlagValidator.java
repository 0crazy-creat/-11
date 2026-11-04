package com.csp.ctf.judge;

/**
 * Flag 判题策略接口（策略模式）。
 *
 * <p>对齐 CTFd 的 challenge type 与 Rust-CTF 的 checker 抽象：
 * 判题差异全部封装在实现类，{@link FlagValidatorRegistry} 按
 * question.ext.judgeMode 路由到具体策略。</p>
 */
public interface FlagValidator {

    /** 该策略支持的判题模式。 */
    JudgeMode mode();

    /**
     * 判定提交是否正确。
     *
     * @param ctx 判题上下文（含题目、队伍、原始提交）
     * @return true 表示答案正确
     */
    boolean validate(JudgeContext ctx);

    /**
     * 期望 flag（用于动态 flag 下发到靶机环境变量）。
     * 仅 DYNAMIC 策略有实际值；其余返回题目配置的静态值或 null。
     */
    default String expectedFlag(JudgeContext ctx) {
        return ctx.getQuestion() == null ? null : ctx.getQuestion().getFlag();
    }
}