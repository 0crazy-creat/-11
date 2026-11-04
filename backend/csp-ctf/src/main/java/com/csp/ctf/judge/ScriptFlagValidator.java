package com.csp.ctf.judge;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 脚本判题：把提交内容转发给题目配置的外部校验器（HTTP），由其返回是否通过。
 *
 * <p>对齐 CTFd 的 script 与 Rust-CTF 的 external checker：
 * 复杂题目（多步利用、需比对服务器状态、需执行容器内 check 脚本）无法用静态字符串描述，
 * 统一收敛为“把上下文 POST 给校验器，读回 correct 布尔值”。</p>
 *
 * <p>安全与稳定性：</p>
 * <ul>
 *   <li>超时由 ext.script.timeoutMs 控制（默认 2s，上限 10s）；</li>
 *   <li>校验器不可用或返回非 2xx 一律判错（fail-closed），绝不因外部故障放行；</li>
 *   <li>不向前端暴露校验器地址。</li>
 * </ul>
 */
@Slf4j
@Component
public class ScriptFlagValidator implements FlagValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_TIMEOUT_MS = 10_000;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @Override
    public JudgeMode mode() {
        return JudgeMode.SCRIPT;
    }

    @Override
    public boolean validate(JudgeContext ctx) {
        QuestionExt ext = ctx.getExt();
        QuestionExt.Script cfg = ext == null ? null : ext.getScript();
        if (cfg == null || cfg.getUrl() == null || cfg.getUrl().isBlank()) {
            log.warn("SCRIPT 判题缺少校验器地址 question={}，按未通过处理",
                    ctx.getQuestion() == null ? null : ctx.getQuestion().getId());
            return false;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contestId", ctx.getContestId());
        body.put("teamId", ctx.getTeamId());
        body.put("userId", ctx.getUserId());
        body.put("questionId", ctx.getQuestion() == null ? null : ctx.getQuestion().getId());
        body.put("submitted", ctx.getSubmitted());
        // 仅动态 flag 才把期望值交给校验器；静态题不下发明文答案
        if (ext.mode() == JudgeMode.DYNAMIC) {
            body.put("expected", expectedFlag(ctx));
        }

        int timeoutMs = cfg.getTimeoutMs() == null ? 2000 : Math.min(Math.max(cfg.getTimeoutMs(), 200), MAX_TIMEOUT_MS);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(cfg.getUrl().trim()))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.warn("脚本判题校验器返回 {} question={}", resp.statusCode(), body.get("questionId"));
                return false;
            }
            Map<?, ?> json = MAPPER.readValue(resp.body(), Map.class);
            return Boolean.TRUE.equals(json.get("correct")) || Boolean.TRUE.equals(json.get("pass"));
        } catch (Exception e) {
            log.warn("脚本判题调用失败 question={}: {}", body.get("questionId"), e.getMessage());
            return false;
        }
    }
}
