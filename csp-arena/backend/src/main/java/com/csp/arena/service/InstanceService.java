package com.csp.arena.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.arena.config.BizException;
import com.csp.arena.dto.CreateInstanceCmd;
import com.csp.arena.dto.InstanceVO;
import com.csp.arena.dto.StatsVO;
import com.csp.arena.entity.*;
import com.csp.arena.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** 靶机实例服务：创建 / 重置 / 销毁 / 巡检 / 统计。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstanceService {

    private final ArenaInstanceMapper instanceMapper;
    private final ArenaTemplateMapper templateMapper;
    private final ArenaActionLogMapper logMapper;
    private final DockerScheduler scheduler;

    private static final Map<String, String> STATUS_TEXT = Map.of(
            "PENDING", "待启动", "RUNNING", "运行中", "STOPPED", "已停止",
            "ERROR", "异常", "DESTROYED", "已销毁");

    // ---------------- 查询 ----------------

    public Page<InstanceVO> page(int page, int size, Long templateId, String status,
                                 Long ownerId, Long contestId, Long teamId) {
        LambdaQueryWrapper<ArenaInstance> q = new LambdaQueryWrapper<>();
        if (templateId != null) q.eq(ArenaInstance::getTemplateId, templateId);
        if (status != null && !status.isBlank()) q.eq(ArenaInstance::getStatus, status);
        if (ownerId != null) q.eq(ArenaInstance::getOwnerId, ownerId);
        if (contestId != null) q.eq(ArenaInstance::getContestId, contestId);
        if (teamId != null) q.eq(ArenaInstance::getTeamId, teamId);
        q.orderByDesc(ArenaInstance::getCreatedAt);

        Page<ArenaInstance> raw = instanceMapper.selectPage(new Page<>(page, size), q);
        Map<Long, ArenaTemplate> tplCache = loadTemplates(raw.getRecords());
        Page<InstanceVO> out = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        out.setRecords(raw.getRecords().stream()
                .map(i -> toVO(i, tplCache.get(i.getTemplateId()), false)).toList());
        return out;
    }

    public InstanceVO detail(Long id, boolean showFlag) {
        ArenaInstance i = requireInstance(id);
        ArenaTemplate t = templateMapper.selectById(i.getTemplateId());
        return toVO(i, t, showFlag);
    }

    public List<InstanceVO> listByOwner(Long ownerId) {
        List<ArenaInstance> list = instanceMapper.selectList(new LambdaQueryWrapper<ArenaInstance>()
                .eq(ArenaInstance::getOwnerId, ownerId)
                .ne(ArenaInstance::getStatus, "DESTROYED")
                .orderByDesc(ArenaInstance::getCreatedAt));
        Map<Long, ArenaTemplate> cache = loadTemplates(list);
        return list.stream().map(i -> toVO(i, cache.get(i.getTemplateId()), true)).toList();
    }

    // ---------------- 创建 / 重置 / 销毁 ----------------

    @Transactional(rollbackFor = Exception.class)
    public InstanceVO create(CreateInstanceCmd cmd, Long ownerId, String ownerName) {
        long start = System.currentTimeMillis();
        ArenaTemplate t = templateMapper.selectById(cmd.getTemplateId());
        if (t == null) throw BizException.notFound("靶场模板");
        if (t.getStatus() == null || t.getStatus() != 1) throw new BizException(409, "模板已停用");

        // 同一用户同一模板最多 2 个活跃实例
        Long active = instanceMapper.selectCount(new LambdaQueryWrapper<ArenaInstance>()
                .eq(ArenaInstance::getOwnerId, ownerId)
                .eq(ArenaInstance::getTemplateId, t.getId())
                .in(ArenaInstance::getStatus, List.of("PENDING", "RUNNING")));
        if (active != null && active >= 2) {
            throw new BizException(409, "该模板活跃实例已达上限（2 个），请先销毁旧实例");
        }

        String flag = cmd.getFlag() != null && !cmd.getFlag().isBlank()
                ? cmd.getFlag()
                : (t.getDefaultFlag() != null && !t.getDefaultFlag().isBlank()
                    ? t.getDefaultFlag() : genFlag());
        int ttl = cmd.getTtlMinutes() != null ? cmd.getTtlMinutes()
                : (t.getTtlMinutes() != null ? t.getTtlMinutes() : 120);

        String cname = "arena-" + t.getCode() + "-" + System.currentTimeMillis() % 1000000;
        Map<String, String> env = new HashMap<>();
        env.put("FLAG", flag);
        env.put("TEMPLATE_CODE", t.getCode());

        DockerScheduler.Launched l = scheduler.launch(
                t.getImage(), cname, t.getContainerPort(), env,
                t.getCpuLimit(), t.getMemoryLimit(), cmd.getContestId(), -1);

        ArenaInstance i = new ArenaInstance();
        i.setTemplateId(t.getId());
        i.setTemplateCode(t.getCode());
        i.setOwnerId(ownerId);
        i.setOwnerName(ownerName);
        i.setContestId(cmd.getContestId());
        i.setTeamId(cmd.getTeamId());
        i.setContainerId(l.containerId());
        i.setContainerName(l.containerName());
        i.setImage(t.getImage());
        i.setHostIp(l.hostIp());
        i.setHostPort(l.hostPort());
        i.setAccessUrl(l.accessUrl());
        i.setFlag(flag);
        i.setStatus("RUNNING");
        i.setHealth(1);
        i.setExpiresAt(LocalDateTime.now().plusMinutes(ttl));
        i.setLastCheckAt(LocalDateTime.now());
        i.setCreatedBy(ownerId);
        i.setUpdatedBy(ownerId);
        instanceMapper.insert(i);

        audit(i.getId(), "CREATE", ownerId, ownerName,
                "template=" + t.getCode() + " image=" + t.getImage()
                        + " port=" + l.hostPort() + " simulated=" + l.simulated(),
                true, System.currentTimeMillis() - start);
        log.info("靶机创建 id={} template={} url={} simulated={}",
                i.getId(), t.getCode(), l.accessUrl(), l.simulated());
        return toVO(i, t, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public InstanceVO reset(Long id, Long actorId, String actorName) {
        long start = System.currentTimeMillis();
        ArenaInstance i = requireInstance(id);
        ArenaTemplate t = templateMapper.selectById(i.getTemplateId());
        if (t == null) throw BizException.notFound("靶场模板");

        String newFlag = genFlag();
        scheduler.remove(i.getContainerId());

        Map<String, String> env = new HashMap<>();
        env.put("FLAG", newFlag);
        env.put("TEMPLATE_CODE", t.getCode());
        DockerScheduler.Launched l = scheduler.launch(
                t.getImage(), i.getContainerName(), t.getContainerPort(), env,
                t.getCpuLimit(), t.getMemoryLimit(), i.getContestId(), i.getHostPort());

        i.setContainerId(l.containerId());
        i.setHostPort(l.hostPort());
        i.setAccessUrl(l.accessUrl());
        i.setFlag(newFlag);
        i.setStatus("RUNNING");
        i.setHealth(1);
        i.setLastCheckAt(LocalDateTime.now());
        i.setUpdatedBy(actorId);
        instanceMapper.updateById(i);

        audit(id, "RESET", actorId, actorName, "newFlag=" + newFlag,
                true, System.currentTimeMillis() - start);
        return toVO(i, t, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean destroy(Long id, Long actorId, String actorName) {
        long start = System.currentTimeMillis();
        ArenaInstance i = requireInstance(id);
        scheduler.stop(i.getContainerId());
        scheduler.remove(i.getContainerId());
        i.setStatus("DESTROYED");
        i.setUpdatedBy(actorId);
        instanceMapper.updateById(i);
        audit(id, "DESTROY", actorId, actorName, "container=" + i.getContainerName(),
                true, System.currentTimeMillis() - start);
        log.info("靶机销毁 id={}", id);
        return true;
    }

    public int destroyAll(Long ownerId, Long contestId) {
        LambdaQueryWrapper<ArenaInstance> q = new LambdaQueryWrapper<ArenaInstance>()
                .in(ArenaInstance::getStatus, List.of("PENDING", "RUNNING", "STOPPED", "ERROR"));
        if (ownerId != null) q.eq(ArenaInstance::getOwnerId, ownerId);
        if (contestId != null) q.eq(ArenaInstance::getContestId, contestId);
        List<ArenaInstance> list = instanceMapper.selectList(q);
        list.forEach(i -> destroy(i.getId(), ownerId, "batch"));
        return list.size();
    }

    // ---------------- 巡检 ----------------

    public int healthCheckAll() {
        List<ArenaInstance> list = instanceMapper.selectList(new LambdaQueryWrapper<ArenaInstance>()
                .in(ArenaInstance::getStatus, List.of("RUNNING", "ERROR", "PENDING")));
        int healthy = 0;
        for (ArenaInstance i : list) {
            boolean ok = scheduler.isRunning(i.getContainerId());
            i.setHealth(ok ? 1 : 0);
            i.setStatus(ok ? "RUNNING" : "ERROR");
            i.setLastCheckAt(LocalDateTime.now());
            double[] s = scheduler.stats(i.getContainerId());
            i.setCpuPercent(Math.round(s[0] * 100) / 100.0);
            i.setMemUsedMb(Math.round(s[1] * 100) / 100.0);
            instanceMapper.updateById(i);
            if (ok) healthy++;
        }
        return healthy;
    }

    public InstanceVO healthCheckOne(Long id, Long actorId, String actorName) {
        long start = System.currentTimeMillis();
        ArenaInstance i = requireInstance(id);
        boolean ok = scheduler.isRunning(i.getContainerId());
        i.setHealth(ok ? 1 : 0);
        i.setStatus(ok ? "RUNNING" : "ERROR");
        i.setLastCheckAt(LocalDateTime.now());
        double[] s = scheduler.stats(i.getContainerId());
        i.setCpuPercent(Math.round(s[0] * 100) / 100.0);
        i.setMemUsedMb(Math.round(s[1] * 100) / 100.0);
        instanceMapper.updateById(i);
        audit(id, "HEALTH_CHECK", actorId, actorName, "healthy=" + ok,
                true, System.currentTimeMillis() - start);
        return toVO(i, templateMapper.selectById(i.getTemplateId()), true);
    }

    /** 回收过期实例（定时任务调用）。 */
    public int reclaimExpired() {
        List<ArenaInstance> expired = instanceMapper.selectList(new LambdaQueryWrapper<ArenaInstance>()
                .in(ArenaInstance::getStatus, List.of("RUNNING", "PENDING", "STOPPED", "ERROR"))
                .isNotNull(ArenaInstance::getExpiresAt)
                .lt(ArenaInstance::getExpiresAt, LocalDateTime.now()));
        for (ArenaInstance i : expired) {
            destroy(i.getId(), null, "auto-reclaim");
        }
        return expired.size();
    }

    // ---------------- 统计 ----------------

    public StatsVO stats() {
        List<ArenaInstance> all = instanceMapper.selectList(new LambdaQueryWrapper<ArenaInstance>()
                .ne(ArenaInstance::getStatus, "DESTROYED"));
        long running = all.stream().filter(i -> "RUNNING".equals(i.getStatus())).count();
        long stopped = all.stream().filter(i -> "STOPPED".equals(i.getStatus())).count();
        long error = all.stream().filter(i -> "ERROR".equals(i.getStatus())).count();
        long healthy = all.stream().filter(i -> i.getHealth() != null && i.getHealth() == 1).count();

        Map<Long, ArenaTemplate> tpl = loadTemplates(all);
        Map<String, Long> byCat = new LinkedHashMap<>();
        for (ArenaInstance i : all) {
            ArenaTemplate t = tpl.get(i.getTemplateId());
            String cat = t == null ? "UNKNOWN" : t.getCategory();
            byCat.merge(cat, 1L, Long::sum);
        }
        Map<String, Long> byStatus = new LinkedHashMap<>();
        all.forEach(i -> byStatus.merge(i.getStatus(), 1L, Long::sum));

        double cpu = all.stream().mapToDouble(i -> i.getCpuPercent() == null ? 0 : i.getCpuPercent()).sum();
        double mem = all.stream().mapToDouble(i -> i.getMemUsedMb() == null ? 0 : i.getMemUsedMb()).sum();

        return StatsVO.builder()
                .templateCount(templateMapper.selectCount(null))
                .instanceCount(all.size())
                .runningCount(running).stoppedCount(stopped).errorCount(error)
                .healthyCount(healthy)
                .categoryDistribution(byCat).statusDistribution(byStatus)
                .totalCpuPercent(Math.round(cpu * 100) / 100).totalMemMb(Math.round(mem * 100) / 100.0)
                .build();
    }

    public List<Map<String, Object>> actionLogs(Long instanceId, int limit) {
        LambdaQueryWrapper<ArenaActionLog> q = new LambdaQueryWrapper<>();
        if (instanceId != null) q.eq(ArenaActionLog::getInstanceId, instanceId);
        q.orderByDesc(ArenaActionLog::getCreatedAt).last("LIMIT " + Math.min(Math.max(limit, 1), 200));
        return logMapper.selectList(q).stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId());
            m.put("instanceId", l.getInstanceId());
            m.put("action", l.getAction());
            m.put("actionText", Map.of("CREATE", "创建", "RESET", "重置", "DESTROY", "销毁",
                    "HEALTH_CHECK", "巡检", "EXTEND", "续期").getOrDefault(l.getAction(), l.getAction()));
            m.put("actorName", l.getActorName());
            m.put("detail", l.getDetail());
            m.put("success", l.getSuccess());
            m.put("costMs", l.getCostMs());
            m.put("createdAt", l.getCreatedAt());
            return m;
        }).toList();
    }

    // ---------------- 内部 ----------------

    private ArenaInstance requireInstance(Long id) {
        ArenaInstance i = instanceMapper.selectById(id);
        if (i == null) throw BizException.notFound("靶机实例");
        return i;
    }

    private Map<Long, ArenaTemplate> loadTemplates(List<ArenaInstance> list) {
        Set<Long> ids = list.stream().map(ArenaInstance::getTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return templateMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ArenaTemplate::getId, t -> t));
    }

    private InstanceVO toVO(ArenaInstance i, ArenaTemplate t, boolean showFlag) {
        return InstanceVO.builder()
                .id(i.getId())
                .templateId(i.getTemplateId())
                .templateCode(i.getTemplateCode())
                .templateName(t == null ? null : t.getName())
                .category(t == null ? null : t.getCategory())
                .difficulty(t == null ? null : t.getDifficulty())
                .ownerId(i.getOwnerId())
                .ownerName(i.getOwnerName())
                .contestId(i.getContestId())
                .teamId(i.getTeamId())
                .accessUrl(i.getAccessUrl())
                .hostIp(i.getHostIp())
                .hostPort(i.getHostPort())
                .status(i.getStatus())
                .health(i.getHealth())
                .cpuPercent(i.getCpuPercent())
                .memUsedMb(i.getMemUsedMb())
                .expiresAt(i.getExpiresAt())
                .createdAt(i.getCreatedAt())
                .lastCheckAt(i.getLastCheckAt())
                .flag(showFlag ? i.getFlag() : null)
                .build();
    }

    private void audit(Long instanceId, String action, Long actorId, String actorName,
                       String detail, boolean success, long costMs) {
        try {
            ArenaActionLog l = new ArenaActionLog();
            l.setInstanceId(instanceId);
            l.setAction(action);
            l.setActorId(actorId);
            l.setActorName(actorName);
            l.setDetail(detail);
            l.setSuccess(success ? 1 : 0);
            l.setCostMs(costMs);
            l.setCreatedAt(LocalDateTime.now());
            logMapper.insert(l);
        } catch (Exception e) {
            log.warn("写审计日志失败: {}", e.getMessage());
        }
    }

    private String genFlag() {
        return "flag{" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "}";
    }
}
