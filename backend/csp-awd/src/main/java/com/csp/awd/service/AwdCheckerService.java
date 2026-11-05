package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.entity.AwdChecker;
import com.csp.awd.entity.AwdTarget;
import com.csp.awd.mapper.AwdCheckerMapper;
import com.csp.awd.mapper.AwdTargetMapper;
import com.csp.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AWD checker：按题目配置检测靶机服务可用性。
 *
 * <p>检测从后端容器发起，目标为靶机映射到宿主机的端口（host.docker.internal:port），
 * 等价于「选手能否访问到该服务」。结果回写 awd_target.service_ok / last_check_msg。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwdCheckerService {

    private final AwdCheckerMapper checkerMapper;
    private final AwdTargetMapper targetMapper;

    private static final String CHECK_HOST = "host.docker.internal";

    public List<AwdChecker> list(Long contestId) {
        return checkerMapper.selectList(new LambdaQueryWrapper<AwdChecker>()
                .eq(AwdChecker::getContestId, contestId)
                .orderByAsc(AwdChecker::getQuestionId));
    }

    public AwdChecker save(AwdChecker cmd) {
        if (cmd.getContestId() == null || cmd.getQuestionId() == null) {
            throw new BizException(400, "contestId / questionId 不能为空");
        }
        if (cmd.getCheckType() == null || cmd.getCheckType().isBlank()) {
            cmd.setCheckType("HTTP");
        }
        if (cmd.getTargetPath() == null || cmd.getTargetPath().isBlank()) {
            cmd.setTargetPath("/");
        }
        if (cmd.getExpectStatus() == null) {
            cmd.setExpectStatus(200);
        }
        if (cmd.getTimeoutMs() == null || cmd.getTimeoutMs() <= 0) {
            cmd.setTimeoutMs(3000);
        }
        if (cmd.getEnabled() == null) {
            cmd.setEnabled(1);
        }
        if (cmd.getId() == null) {
            checkerMapper.insert(cmd);
        } else {
            checkerMapper.updateById(cmd);
        }
        return cmd;
    }

    public void delete(Long id) {
        checkerMapper.deleteById(id);
    }

    /** 对赛事下所有靶机执行一轮检测。 */
    public Map<String, Object> runContest(Long contestId) {
        List<AwdChecker> checkers = checkerMapper.selectList(new LambdaQueryWrapper<AwdChecker>()
                .eq(AwdChecker::getContestId, contestId)
                .eq(AwdChecker::getEnabled, 1));
        Map<Long, AwdChecker> byQuestion = checkers.stream()
                .collect(Collectors.toMap(AwdChecker::getQuestionId, c -> c, (a, b) -> a));

        List<AwdTarget> targets = targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId));

        int up = 0, down = 0, skipped = 0;
        for (AwdTarget t : targets) {
            AwdChecker checker = byQuestion.get(t.getQuestionId());
            if (checker == null) {
                skipped++;
                continue;
            }
            CheckResult r = checkOne(checker, t);
            t.setServiceOk(r.ok ? 1 : 0);
            t.setLastCheckMsg(r.message);
            t.setHealth(r.ok ? 1 : 0);
            t.setLastCheckAt(LocalDateTime.now());
            targetMapper.updateById(t);
            if (r.ok) up++; else down++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", targets.size());
        result.put("up", up);
        result.put("down", down);
        result.put("skipped", skipped);
        result.put("checkedAt", LocalDateTime.now().toString());
        log.info("checker 完成 contest={} up={} down={} skipped={}", contestId, up, down, skipped);
        return result;
    }

    /** 定时巡检：对配置了 checker 的赛事每 60 秒跑一轮。 */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void scheduledCheck() {
        List<AwdChecker> all;
        try {
            all = checkerMapper.selectList(new LambdaQueryWrapper<AwdChecker>()
                    .eq(AwdChecker::getEnabled, 1));
        } catch (Exception e) {
            return;
        }
        Set<Long> contestIds = all.stream().map(AwdChecker::getContestId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        for (Long cid : contestIds) {
            try {
                runContest(cid);
            } catch (Exception e) {
                log.warn("checker 定时巡检失败 contest={}: {}", cid, e.getMessage());
            }
        }
    }

    private CheckResult checkOne(AwdChecker checker, AwdTarget target) {
        Integer port = target.getPort();
        if (port == null || port <= 0) {
            return new CheckResult(false, "靶机未分配端口");
        }
        int timeout = checker.getTimeoutMs() == null ? 3000 : checker.getTimeoutMs();
        if ("TCP".equalsIgnoreCase(checker.getCheckType())) {
            return checkTcp(port, timeout);
        }
        return checkHttp(port, checker.getTargetPath(), checker.getExpectStatus(), timeout);
    }

    private CheckResult checkTcp(int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(CHECK_HOST, port), timeout);
            return new CheckResult(true, "TCP " + CHECK_HOST + ":" + port + " 可连接");
        } catch (IOException e) {
            return new CheckResult(false, "TCP 连接失败：" + e.getMessage());
        }
    }

    private CheckResult checkHttp(int port, String path, Integer expect, int timeout) {
        HttpURLConnection conn = null;
        try {
            String p = (path == null || path.isBlank()) ? "/" : path;
            if (!p.startsWith("/")) p = "/" + p;
            URL url = new URL("http://" + CHECK_HOST + ":" + port + p);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setInstanceFollowRedirects(false);
            int code = conn.getResponseCode();
            int want = expect == null ? 200 : expect;
            boolean ok = code == want;
            return new CheckResult(ok, "HTTP " + code + (ok ? " 正常" : "，期望 " + want));
        } catch (Exception e) {
            return new CheckResult(false, "HTTP 检测失败：" + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private record CheckResult(boolean ok, String message) {
    }
}
