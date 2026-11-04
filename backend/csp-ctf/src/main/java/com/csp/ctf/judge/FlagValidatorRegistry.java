package com.csp.ctf.judge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 判题策略注册表：按 {@code question.ext.judgeMode} 路由到具体 {@link FlagValidator}。
 *
 * <p>对齐 CTFd 的 challenge type 分派与 Rust-CTF 的 checker registry：
 * 新增判题方式只需实现 {@link FlagValidator} 并声明 {@link FlagValidator#mode()}，
 * Spring 自动注入后即生效，主流程无需改动。</p>
 *
 * <p>降级策略：未注册的模式回退到 STATIC，保证历史题目（ext 为空）仍可判分。</p>
 */
@Slf4j
@Component
public class FlagValidatorRegistry {

    private final Map<JudgeMode, FlagValidator> validators = new EnumMap<>(JudgeMode.class);
    private final FlagValidator fallback;

    public FlagValidatorRegistry(List<FlagValidator> beans) {
        FlagValidator staticValidator = null;
        for (FlagValidator v : beans) {
            if (v.mode() == null) continue;
            validators.put(v.mode(), v);
            if (v.mode() == JudgeMode.STATIC) staticValidator = v;
        }
        this.fallback = staticValidator;
        if (fallback == null) {
            log.warn("未注册 STATIC 判题策略，judgeMode 未知时将无法判分");
        }
        log.info("FlagValidator 策略注册完成: {}", validators.keySet());
    }

    /** 按模式取策略；未注册时回退 STATIC。 */
    public FlagValidator of(JudgeMode mode) {
        FlagValidator v = mode == null ? null : validators.get(mode);
        if (v != null) return v;
        if (mode != null) log.warn("judgeMode={} 未注册策略，回退 STATIC", mode);
        return fallback;
    }

    /** 按题目 ext 取策略（ext 为 null 视为静态题）。 */
    public FlagValidator of(QuestionExt ext) {
        return of(ext == null ? JudgeMode.STATIC : ext.mode());
    }
}