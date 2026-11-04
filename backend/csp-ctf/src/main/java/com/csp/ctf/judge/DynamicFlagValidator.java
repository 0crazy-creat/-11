package com.csp.ctf.judge;

import com.csp.common.util.FlagUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 动态 flag 判题：每个「赛事 + 战队 + 题目」三元组派生唯一 flag，比对派生值。
 *
 * <p>对齐 CTFd 的 dynamic flag 与 Rust-CTF 的 per-team flag：
 * 队伍之间 flag 互不相同，泄漏一队的 flag 不影响其他队，天然抑制“抄答案”。</p>
 *
 * <p>派生规则：{@code flag{flag_<contestId>_<teamId>_<questionId>_<指纹>}}，
 * 指纹 = HMAC-SHA256(secret, contest:team:question) 前 16 位十六进制。
 * 该值<b>不落库</b>，服务重启后同队同题仍得到同一 flag，避免靶机中 flag 失配。</p>
 */
@Slf4j
@Component
public class DynamicFlagValidator implements FlagValidator {

    /** 独立派生密钥（与 JWT 密钥分离），可用 csp.ctf.dynamic-flag-secret 覆盖。 */
    @Value("${csp.ctf.dynamic-flag-secret:csp-dynamic-flag-derive-key-2026}")
    private String secret;

    /** flag 前缀，默认 flag。 */
    @Value("${csp.ctf.flag-prefix:flag}")
    private String prefix;

    @Override
    public JudgeMode mode() {
        return JudgeMode.DYNAMIC;
    }

    @Override
    public boolean validate(JudgeContext ctx) {
        String submitted = FlagUtils.normalize(ctx.getSubmitted());
        if (submitted.isEmpty() || ctx.getQuestion() == null) return false;
        if (ctx.getTeamId() == null) {
            // 动态 flag 依赖队伍身份，缺失时不放行，避免退化为“人人可解”
            log.warn("动态 flag 判题缺少 teamId question={}", ctx.getQuestion().getId());
            return false;
        }
        String expected = derive(ctx.getContestId(), ctx.getTeamId(), ctx.getQuestion().getId());
        return FlagUtils.safeEquals(expected, submitted);
    }

    @Override
    public String expectedFlag(JudgeContext ctx) {
        if (ctx.getQuestion() == null || ctx.getTeamId() == null) return null;
        return derive(ctx.getContestId(), ctx.getTeamId(), ctx.getQuestion().getId());
    }

    /** 派生某队某题的唯一 flag（同时用于注入靶机环境变量 FLAG）。 */
    public String derive(Long contestId, Long teamId, Long questionId) {
        String material = contestId + ":" + teamId + ":" + questionId;
        String fp = FlagUtils.hmacSha256Hex(secret, material).substring(0, 16);
        return prefix + "{" + prefix + "_" + contestId + "_" + teamId + "_" + questionId + "_" + fp + "}";
    }
}
