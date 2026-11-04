package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.entity.AwdTarget;
import com.csp.awd.entity.AwdTrafficAnalysis;
import com.csp.awd.entity.AwdTrafficCapture;
import com.csp.awd.mapper.AwdTargetMapper;
import com.csp.awd.mapper.AwdTrafficAnalysisMapper;
import com.csp.awd.mapper.AwdTrafficCaptureMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AWD 流量取证服务：捕获、存储、分析网络流量。
 * 
 * <p>使用 tcpdump 在每轮比赛中捕获各战队靶机的网络流量，
 * 存储为 pcap 文件并记录元数据，支持后续自动化分析。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficCaptureService {

    /** 流量捕获进程管理（key: captureId） */
    private final Map<Long, Process> captureProcesses = new ConcurrentHashMap<>();

    @Value("${awd.traffic.capture.duration:300}")
    private int captureDuration;

    @Value("${awd.traffic.capture.filter:tcp or udp or icmp}")
    private String captureFilter;

    @Value("${awd.traffic.storage.path:/tmp/awd-traffic}")
    private String storagePath;

    @Value("${awd.traffic.retention.days:30}")
    private int retentionDays;

    private final AwdTrafficCaptureMapper captureMapper;
    private final AwdTrafficAnalysisMapper analysisMapper;
    private final AwdTargetMapper targetMapper;
    private final ObjectMapper objectMapper;

    /**
     * 启动流量捕获（异步执行）。
     *
     * @param contestId 赛事ID
     * @param roundNo   轮次
     * @param teamId    战队ID
     * @param targetId  靶机ID
     * @return 捕获记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long startCapture(Long contestId, Integer roundNo, Long teamId, Long targetId) {
        // 查询靶机信息
        AwdTarget target = targetMapper.selectById(targetId);
        if (target == null) {
            throw new IllegalArgumentException("靶机不存在: " + targetId);
        }

        // 检查是否已有捕获记录
        AwdTrafficCapture existing = captureMapper.selectOne(new LambdaQueryWrapper<AwdTrafficCapture>()
                .eq(AwdTrafficCapture::getContestId, contestId)
                .eq(AwdTrafficCapture::getRoundNo, roundNo)
                .eq(AwdTrafficCapture::getTeamId, teamId)
                .eq(AwdTrafficCapture::getTargetId, targetId));
        
        if (existing != null) {
            if ("CAPTURING".equals(existing.getStatus())) {
                log.warn("流量捕获已在进行中: captureId={}", existing.getId());
                return existing.getId();
            }
            // 重新捕获：删除旧记录
            captureMapper.deleteById(existing.getId());
        }

        // 创建捕获记录
        AwdTrafficCapture capture = new AwdTrafficCapture();
        capture.setContestId(contestId);
        capture.setRoundNo(roundNo);
        capture.setTeamId(teamId);
        capture.setTargetId(targetId);
        capture.setStorageType("LOCAL");
        capture.setStatus("CAPTURING");
        capture.setStartedAt(LocalDateTime.now());
        capture.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));
        capture.setCreatedAt(LocalDateTime.now());
        
        // 生成文件路径
        String fileName = String.format("contest_%d_round_%d_team_%d_target_%d_%d.pcap",
                contestId, roundNo, teamId, targetId, System.currentTimeMillis());
        Path filePath = Paths.get(storagePath, String.valueOf(contestId), fileName);
        capture.setCaptureFile(filePath.toString());
        
        captureMapper.insert(capture);
        log.info("流量捕获已启动: captureId={}, target={}, file={}", 
                capture.getId(), target.getIp(), filePath);

        // 异步执行 tcpdump
        executeTcpdumpAsync(capture.getId(), target.getIp(), filePath);

        return capture.getId();
    }

    /**
     * 异步执行 tcpdump 命令。
     */
    @Async
    public void executeTcpdumpAsync(Long captureId, String targetIp, Path filePath) {
        try {
            // 确保目录存在
            Files.createDirectories(filePath.getParent());

            // 构建 tcpdump 命令
            List<String> command = List.of(
                    "tcpdump",
                    "-i", "any",                    // 监听所有网卡
                    "-w", filePath.toString(),      // 写入文件
                    "-c", "100000",                 // 最多捕获10万个包
                    "-G", String.valueOf(captureDuration),  // 捕获时长
                    "-W", "1",                      // 只生成一个文件
                    captureFilter                   // 过滤规则
            );

            log.debug("执行 tcpdump: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            captureProcesses.put(captureId, process);

            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[tcpdump-{}] {}", captureId, line);
                }
            }

            // 等待完成
            boolean completed = process.waitFor(captureDuration + 30, TimeUnit.SECONDS);
            captureProcesses.remove(captureId);

            if (!completed) {
                process.destroyForcibly();
                log.warn("tcpdump 超时，强制终止: captureId={}", captureId);
            }

            // 检查退出码
            int exitCode = process.exitValue();
            if (exitCode != 0 && exitCode != 143) {  // 143 = SIGTERM
                throw new RuntimeException("tcpdump 退出码异常: " + exitCode);
            }

            // 更新捕获记录
            File file = filePath.toFile();
            if (file.exists() && file.length() > 0) {
                captureMapper.markCompleted(captureId, file.length(), estimatePacketCount(file.length()));
                log.info("流量捕获完成: captureId={}, size={} bytes", captureId, file.length());
                
                // 触发自动分析
                triggerAnalysis(captureId);
            } else {
                captureMapper.markFailed(captureId, "捕获文件为空或不存在");
                log.warn("流量捕获失败: captureId={}, file={}", captureId, filePath);
            }

        } catch (Exception e) {
            log.error("流量捕获异常: captureId={}", captureId, e);
            captureMapper.markFailed(captureId, e.getMessage());
            captureProcesses.remove(captureId);
        }
    }

    /**
     * 停止流量捕获。
     */
    @Transactional(rollbackFor = Exception.class)
    public void stopCapture(Long captureId) {
        Process process = captureProcesses.get(captureId);
        if (process != null && process.isAlive()) {
            process.destroy();
            log.info("已发送停止信号: captureId={}", captureId);
            
            // 等待进程结束
            try {
                boolean stopped = process.waitFor(10, TimeUnit.SECONDS);
                if (!stopped) {
                    process.destroyForcibly();
                    log.warn("强制终止捕获进程: captureId={}", captureId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            captureProcesses.remove(captureId);
        }

        // 更新状态
        AwdTrafficCapture capture = captureMapper.selectById(captureId);
        if (capture != null && "CAPTURING".equals(capture.getStatus())) {
            File file = new File(capture.getCaptureFile());
            if (file.exists()) {
                captureMapper.markCompleted(captureId, file.length(), estimatePacketCount(file.length()));
            } else {
                captureMapper.markFailed(captureId, "文件不存在");
            }
        }
    }

    /**
     * 触发流量分析（异步）。
     */
    @Async
    public void triggerAnalysis(Long captureId) {
        try {
            AwdTrafficCapture capture = captureMapper.selectById(captureId);
            if (capture == null || !"COMPLETED".equals(capture.getStatus())) {
                log.warn("跳过分析：捕获未完成 captureId={}", captureId);
                return;
            }

            log.info("开始流量分析: captureId={}", captureId);

            // TODO: 集成实际的分析工具（如 tshark、Zeek、自定义规则引擎）
            // 这里仅创建示例分析结果
            AwdTrafficAnalysis analysis = new AwdTrafficAnalysis();
            analysis.setCaptureId(captureId);
            analysis.setAnalysisType("ATTACK_DETECT");
            analysis.setSeverity("INFO");
            analysis.setSummary("流量分析功能待实现，当前为占位结果");
            analysis.setConfidence(BigDecimal.ZERO);
            analysis.setAnalyzer("PLACEHOLDER");
            analysis.setAnalyzedAt(LocalDateTime.now());
            
            Map<String, Object> findings = new HashMap<>();
            findings.put("totalPackets", estimatePacketCount(capture.getFileSize()));
            findings.put("duration", capture.getDuration());
            findings.put("note", "需要集成 tshark 或 Zeek 进行深度分析");
            analysis.setFindings(objectMapper.writeValueAsString(findings));
            
            analysisMapper.insert(analysis);
            log.info("流量分析完成: captureId={}", captureId);

        } catch (Exception e) {
            log.error("流量分析失败: captureId={}", captureId, e);
        }
    }

    /**
     * 查询捕获记录。
     */
    public AwdTrafficCapture getCapture(Long captureId) {
        return captureMapper.selectById(captureId);
    }

    /**
     * 查询某轮某队的所有捕获记录。
     */
    public List<AwdTrafficCapture> listCaptures(Long contestId, Integer roundNo, Long teamId) {
        return captureMapper.selectList(new LambdaQueryWrapper<AwdTrafficCapture>()
                .eq(AwdTrafficCapture::getContestId, contestId)
                .eq(AwdTrafficCapture::getRoundNo, roundNo)
                .eq(AwdTrafficCapture::getTeamId, teamId)
                .orderByDesc(AwdTrafficCapture::getStartedAt));
    }

    /**
     * 查询捕获记录的分析结果。
     */
    public List<AwdTrafficAnalysis> getAnalysisResults(Long captureId) {
        return analysisMapper.selectList(new LambdaQueryWrapper<AwdTrafficAnalysis>()
                .eq(AwdTrafficAnalysis::getCaptureId, captureId)
                .orderByDesc(AwdTrafficAnalysis::getAnalyzedAt));
    }

    /**
     * 清理过期的流量文件。
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpired() {
        List<AwdTrafficCapture> expired = captureMapper.selectList(
                new LambdaQueryWrapper<AwdTrafficCapture>()
                        .lt(AwdTrafficCapture::getExpiresAt, LocalDateTime.now())
                        .in(AwdTrafficCapture::getStatus, "COMPLETED", "FAILED"));
        
        int count = 0;
        for (AwdTrafficCapture capture : expired) {
            try {
                // 删除文件
                File file = new File(capture.getCaptureFile());
                if (file.exists() && file.delete()) {
                    log.info("已删除过期流量文件: {}", capture.getCaptureFile());
                }
                // 删除记录
                captureMapper.deleteById(capture.getId());
                count++;
            } catch (Exception e) {
                log.error("清理失败: captureId={}", capture.getId(), e);
            }
        }
        
        log.info("过期流量清理完成: 共删除 {} 条记录", count);
        return count;
    }

    /**
     * 估算数据包数量（基于文件大小，粗略估算）。
     */
    private int estimatePacketCount(long fileSize) {
        // 假设平均每个包 100 字节（实际应根据 pcap 解析）
        return (int) (fileSize / 100);
    }
}
