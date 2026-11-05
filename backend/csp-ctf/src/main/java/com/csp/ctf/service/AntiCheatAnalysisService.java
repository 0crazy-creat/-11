package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.ctf.entity.CheatAuditLog;
import com.csp.ctf.entity.DeviceFingerprint;
import com.csp.ctf.mapper.CheatAuditLogMapper;
import com.csp.ctf.mapper.DeviceFingerprintMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反作弊深度分析：设备指纹管理、多账号检测、风险评估、审计日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiCheatAnalysisService {

    /** 同一设备最多关联的用户数阈值 */
    private static final int DEVICE_MULTI_USERS_THRESHOLD = 3;
    /** 信任分低于此值标记为可疑 */
    private static final int SUSPICIOUS_TRUST_SCORE = 60;

    private final DeviceFingerprintMapper fingerprintMapper;
    private final CheatAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 记录或更新设备指纹。
     *
     * @param contestId   赛事ID
     * @param userId      用户ID
     * @param deviceId    设备ID（前端生成）
     * @param fingerprint 设备特征（JSON）
     * @param ipAddress   IP地址
     * @param userAgent   浏览器UA
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordFingerprint(Long contestId, Long userId, String deviceId,
                                   String fingerprint, String ipAddress, String userAgent) {
        if (deviceId == null || deviceId.isBlank()) {
            log.debug("设备ID为空，跳过指纹记录");
            return;
        }

        DeviceFingerprint existing = fingerprintMapper.selectOne(new LambdaQueryWrapper<DeviceFingerprint>()
                .eq(DeviceFingerprint::getDeviceId, deviceId)
                .eq(DeviceFingerprint::getUserId, userId)
                .eq(DeviceFingerprint::getContestId, contestId));

        if (existing == null) {
            // 首次出现
            DeviceFingerprint df = new DeviceFingerprint();
            df.setDeviceId(deviceId);
            df.setUserId(userId);
            df.setContestId(contestId);
            df.setFingerprint(normalizeFingerprint(fingerprint, deviceId, ipAddress, userAgent));
            df.setIpAddress(ipAddress);
            df.setUserAgent(userAgent);
            df.setFirstSeenAt(LocalDateTime.now());
            df.setLastSeenAt(LocalDateTime.now());
            df.setTrustScore(100);
            fingerprintMapper.insert(df);
            log.info("新设备指纹已记录 contest={} user={} device={}", contestId, userId, deviceId);
        } else {
            // 更新最后出现时间和IP
            existing.setLastSeenAt(LocalDateTime.now());
            existing.setIpAddress(ipAddress);
            existing.setUserAgent(userAgent);
            existing.setFingerprint(normalizeFingerprint(fingerprint, deviceId, ipAddress, userAgent));
            fingerprintMapper.updateById(existing);
        }

        // 检测多账号风险
        detectMultiAccountRisk(contestId, userId, deviceId, ipAddress);
    }

    /**
     * 归一化设备特征文档。
     *
     * <p>{@code device_fingerprint.fingerprint} 是 {@code JSON NOT NULL}：现有提交入口并不采集
     * 浏览器特征（统一传 null），若不在此补出合法 JSON，插入会抛
     * {@code Field 'fingerprint' doesn't have a default value}，并把调用方（答题判分）的整个事务
     * 一起回滚——表现为「凡是带 deviceId 的提交一律 500」。</p>
     */
    private String normalizeFingerprint(String fingerprint, String deviceId, String ipAddress, String userAgent) {
        if (fingerprint != null && !fingerprint.isBlank()) {
            try {
                objectMapper.readTree(fingerprint);
                return fingerprint;
            } catch (Exception ignored) {
                // 非 JSON 的裸字符串（旧客户端）落到下面的结构化文档
            }
        }
        Map<String, Object> doc = new HashMap<>();
        doc.put("deviceId", deviceId);
        doc.put("ip", ipAddress);
        doc.put("userAgent", userAgent);
        doc.put("source", "server-derived");
        try {
            return objectMapper.writeValueAsString(doc);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 检测多账号风险（同一设备多个用户）。
     */
    private void detectMultiAccountRisk(Long contestId, Long userId, String deviceId, String ipAddress) {
        Integer userCount = fingerprintMapper.countUsersByDevice(deviceId, contestId);
        if (userCount != null && userCount >= DEVICE_MULTI_USERS_THRESHOLD) {
            // 记录高风险事件
            logAudit(contestId, userId, null, "MULTI_DEVICE", "HIGH", 80,
                    Map.of("deviceId", deviceId, "userCount", userCount, "ipAddress", ipAddress),
                    ipAddress, deviceId, "WARNING");

            // 降低该设备的信任分
            List<DeviceFingerprint> relatedFingerprints = fingerprintMapper.selectList(
                    new LambdaQueryWrapper<DeviceFingerprint>()
                            .eq(DeviceFingerprint::getDeviceId, deviceId)
                            .eq(DeviceFingerprint::getContestId, contestId));
            for (DeviceFingerprint fp : relatedFingerprints) {
                int newScore = Math.max(0, fp.getTrustScore() - 20);
                fp.setTrustScore(newScore);
                fp.setRiskFlags(appendRiskFlag(fp.getRiskFlags(), "MULTI_ACCOUNT"));
                fingerprintMapper.updateById(fp);
            }
            log.warn("[反作弊] 检测到设备多账号嫌疑 device={} users={}", deviceId, userCount);
        }
    }

    /**
     * 记录作弊审计日志。
     */
    @Transactional(rollbackFor = Exception.class)
    public void logAudit(Long contestId, Long userId, Long teamId, String auditType,
                          String severity, Integer riskScore, Map<String, Object> detail,
                          String ipAddress, String deviceId, String actionTaken) {
        try {
            CheatAuditLog log = new CheatAuditLog();
            log.setContestId(contestId);
            log.setUserId(userId);
            log.setTeamId(teamId);
            log.setAuditType(auditType);
            log.setSeverity(severity);
            log.setRiskScore(riskScore);
            log.setDetail(objectMapper.writeValueAsString(detail));
            log.setIpAddress(ipAddress);
            log.setDeviceId(deviceId);
            log.setActionTaken(actionTaken);
            log.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            log.error("写入作弊审计日志失败", e);
        }
    }

    /**
     * 查询用户风险摘要。
     */
    public Map<String, Object> getUserRiskSummary(Long contestId, Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // 查询该用户使用的所有设备
        List<String> devices = fingerprintMapper.selectDevicesByUserAndContest(userId, contestId);
        summary.put("deviceCount", devices.size());
        summary.put("devices", devices);

        // 查询该用户的审计日志数量（按类型分组）
        List<CheatAuditLog> logs = auditLogMapper.selectList(new LambdaQueryWrapper<CheatAuditLog>()
                .eq(CheatAuditLog::getContestId, contestId)
                .eq(CheatAuditLog::getUserId, userId)
                .orderByDesc(CheatAuditLog::getCreatedAt));
        
        Map<String, Long> auditTypeCount = new HashMap<>();
        int maxRiskScore = 0;
        for (CheatAuditLog log : logs) {
            auditTypeCount.merge(log.getAuditType(), 1L, Long::sum);
            maxRiskScore = Math.max(maxRiskScore, log.getRiskScore());
        }
        summary.put("auditLogs", auditTypeCount);
        summary.put("maxRiskScore", maxRiskScore);
        summary.put("totalAuditCount", logs.size());

        // 查询设备指纹详情
        List<DeviceFingerprint> fingerprints = fingerprintMapper.selectList(
                new LambdaQueryWrapper<DeviceFingerprint>()
                        .eq(DeviceFingerprint::getContestId, contestId)
                        .eq(DeviceFingerprint::getUserId, userId));
        summary.put("fingerprints", fingerprints);

        return summary;
    }

    /**
     * 查询赛事高风险用户列表。
     */
    public List<CheatAuditLog> getHighRiskUsers(Long contestId, int minRiskScore) {
        return auditLogMapper.selectList(new LambdaQueryWrapper<CheatAuditLog>()
                .eq(CheatAuditLog::getContestId, contestId)
                .ge(CheatAuditLog::getRiskScore, minRiskScore)
                .orderByDesc(CheatAuditLog::getRiskScore)
                .orderByDesc(CheatAuditLog::getCreatedAt));
    }

    private String appendRiskFlag(String existing, String newFlag) {
        if (existing == null || existing.isBlank()) return newFlag;
        if (existing.contains(newFlag)) return existing;
        return existing + "," + newFlag;
    }
}
