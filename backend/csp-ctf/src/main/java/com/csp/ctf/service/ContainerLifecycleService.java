package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.ChallengeContainer;
import com.csp.ctf.entity.FlagContext;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.judge.DynamicFlagValidator;
import com.csp.ctf.judge.JudgeContext;
import com.csp.ctf.judge.JudgeMode;
import com.csp.ctf.judge.QuestionExt;
import com.csp.ctf.mapper.ChallengeContainerMapper;
import com.csp.ctf.mapper.FlagContextMapper;
import com.csp.infra.docker.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 容器生命周期服务（对齐 GZCTF GameInstanceRepository + ContainerRepository）。
 *
 * <p>职责：
 * <ul>
 *   <li>容器创建/销毁/重置/续期 — 持久化到 challenge_container 表</li>
 *   <li>动态 Flag 分配 — 持久化到 flag_context 表</li>
 *   <li>操作限流 — 10 秒冷却 + 每队容器数量上限</li>
 *   <li>状态同步 — 从 Docker 实际状态回写数据库</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerLifecycleService {

    private final ContainerManager containerManager;
    private final ChallengeContainerMapper containerMapper;
    private final FlagContextMapper flagContextMapper;
    private final DynamicFlagValidator dynamicFlagValidator;

    @Value("${csp.docker.container-cooldown-seconds:10}")
    private int cooldownSeconds;

    @Value("${csp.docker.container-limit-per-team:3}")
    private int containerLimitPerTeam;

    @Value("${csp.docker.auto-destroy-on-limit:true}")
    private boolean autoDestroyOnLimit;

    @Value("${csp.docker.default-ttl-minutes:120}")
    private int defaultTtlMinutes;

    /**
     * 为某队某题创建容器。
     * 如果已有 RUNNING 容器则直接返回；否则创建新容器并持久化。
     */
    @Transactional
    public ChallengeContainer createContainer(Long contestId, Long teamId, Long questionId,
                                               Long userId, String image, int exposedPort,
                                               QuestionExt ext, NetworkMode networkMode) {
        String containerName = buildContainerName(contestId, teamId, questionId);

        ChallengeContainer existing = findRunningContainer(contestId, teamId, questionId);
        if (existing != null) {
            ContainerStatus actualStatus = containerManager.inspectStatus(existing.getContainerId());
            if (actualStatus == ContainerStatus.RUNNING || existing.getSimulated()) {
                return existing;
            }
            existing.setStatus(actualStatus.name());
            containerMapper.updateById(existing);
            if (actualStatus == ContainerStatus.DESTROYED || actualStatus == ContainerStatus.ERROR) {
                containerMapper.deleteById(existing.getId());
            } else {
                return existing;
            }
        }

        checkRateLimit(contestId, teamId);

        String flag = resolveFlag(contestId, teamId, questionId, ext);

        ContainerConfig config = ContainerConfig.builder()
                .contestId(contestId)
                .teamId(teamId)
                .questionId(questionId)
                .userId(userId)
                .image(image)
                .exposedPort(exposedPort)
                .flag(flag)
                .networkMode(networkMode != null ? networkMode : NetworkMode.ISOLATED)
                .cpuLimit(ext != null && ext.getDynamicScore() != null ? 1.0 : 0.5)
                .memoryLimit(512)
                .storageLimit(256)
                .ttlMinutes(defaultTtlMinutes)
                .build();

        ContainerResult result = containerManager.create(config);

        ChallengeContainer record = new ChallengeContainer();
        record.setContestId(contestId);
        record.setTeamId(teamId);
        record.setQuestionId(questionId);
        record.setUserId(userId);
        record.setImage(image);
        record.setContainerId(result.getContainerId());
        record.setContainerName(result.getContainerName());
        record.setStatus(result.getStatus().name());
        record.setStartedAt(result.getStartedAt());
        record.setExpectStopAt(result.getExpectStopAt());
        record.setIp(result.getIp());
        record.setPort(result.getPort());
        record.setPublicPort(result.getPublicPort());
        record.setIsProxy(false);
        record.setNetworkMode(config.getNetworkMode().name());
        record.setCpuLimit(config.getCpuLimit());
        record.setMemoryLimit(config.getMemoryLimit());
        record.setStorageLimit(config.getStorageLimit());
        record.setSimulated(result.isSimulated());
        record.setLastOperationAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        containerMapper.insert(record);
        log.info("容器已创建并持久化: contest={} team={} q={} container={} simulated={}",
                contestId, teamId, questionId, result.getContainerId(), result.isSimulated());

        return record;
    }

    /** 销毁容器。 */
    @Transactional
    public boolean destroyContainer(Long contestId, Long teamId, Long questionId) {
        ChallengeContainer container = findContainer(contestId, teamId, questionId);
        if (container == null) return true;

        boolean ok = containerManager.destroy(container.getContainerId());
        container.setStatus(ContainerStatus.DESTROYED.name());
        container.setUpdatedAt(LocalDateTime.now());
        containerMapper.updateById(container);

        log.info("容器已销毁: contest={} team={} q={} container={}",
                contestId, teamId, questionId, container.getContainerId());
        return ok;
    }

    /** 重置容器：销毁后重建。 */
    @Transactional
    public ChallengeContainer resetContainer(Long contestId, Long teamId, Long questionId,
                                              Long userId, String image, int exposedPort,
                                              QuestionExt ext, NetworkMode networkMode) {
        destroyContainer(contestId, teamId, questionId);
        return createContainer(contestId, teamId, questionId, userId, image, exposedPort, ext, networkMode);
    }

    /** 续期容器 TTL。 */
    @Transactional
    public ChallengeContainer extendContainer(Long contestId, Long teamId, Long questionId) {
        ChallengeContainer container = findRunningContainer(contestId, teamId, questionId);
        if (container == null) throw BizException.notFound("运行中的容器");

        LocalDateTime newStopAt = container.getExpectStopAt().plusMinutes(defaultTtlMinutes);
        container.setExpectStopAt(newStopAt);
        container.setUpdatedAt(LocalDateTime.now());
        containerMapper.updateById(container);

        log.info("容器已续期: container={} newStopAt={}", container.getContainerId(), newStopAt);
        return container;
    }

    /** 查询某队某题的容器状态。 */
    public ChallengeContainer getContainer(Long contestId, Long teamId, Long questionId) {
        ChallengeContainer container = findContainer(contestId, teamId, questionId);
        if (container != null && !container.getSimulated()
                && ContainerStatus.RUNNING.name().equals(container.getStatus())) {
            ContainerStatus actual = containerManager.inspectStatus(container.getContainerId());
            if (actual != ContainerStatus.RUNNING) {
                container.setStatus(actual.name());
                containerMapper.updateById(container);
            }
        }
        return container;
    }

    /** 查询赛事下所有运行中容器（管理端用）。 */
    public List<ChallengeContainer> listByContest(Long contestId) {
        return containerMapper.selectList(new LambdaQueryWrapper<ChallengeContainer>()
                .eq(ChallengeContainer::getContestId, contestId)
                .ne(ChallengeContainer::getStatus, "DESTROYED")
                .orderByDesc(ChallengeContainer::getCreatedAt));
    }

    /** 查询即将过期的容器（GC 用）。 */
    public List<ChallengeContainer> getDyingContainers() {
        return containerMapper.selectDyingContainers(LocalDateTime.now());
    }

    /** 强制销毁指定容器（管理端用）。 */
    @Transactional
    public boolean forceDestroy(Long containerDbId) {
        ChallengeContainer container = containerMapper.selectById(containerDbId);
        if (container == null) return true;
        boolean ok = containerManager.destroy(container.getContainerId());
        container.setStatus(ContainerStatus.DESTROYED.name());
        containerMapper.updateById(container);
        return ok;
    }

    // ======================== 内部方法 ========================

    private void checkRateLimit(Long contestId, Long teamId) {
        List<ChallengeContainer> running = containerMapper.selectTeamRunningContainers(contestId, teamId);

        for (ChallengeContainer c : running) {
            if (c.getLastOperationAt() != null
                    && Duration.between(c.getLastOperationAt(), LocalDateTime.now()).getSeconds() < cooldownSeconds) {
                throw new BizException(429, "操作过于频繁，请等待 " + cooldownSeconds + " 秒");
            }
        }

        if (running.size() >= containerLimitPerTeam) {
            if (autoDestroyOnLimit && !running.isEmpty()) {
                ChallengeContainer oldest = running.get(running.size() - 1);
                log.info("自动销毁最旧容器: team={} container={}", teamId, oldest.getContainerId());
                containerManager.destroy(oldest.getContainerId());
                oldest.setStatus(ContainerStatus.DESTROYED.name());
                containerMapper.updateById(oldest);
            } else {
                throw new BizException(429, "容器数量已达上限(" + containerLimitPerTeam + ")，请先销毁旧容器");
            }
        }
    }

    private String resolveFlag(Long contestId, Long teamId, Long questionId, QuestionExt ext) {
        if (ext == null || ext.mode() != JudgeMode.DYNAMIC) return null;

        FlagContext fc = flagContextMapper.selectOne(new LambdaQueryWrapper<FlagContext>()
                .eq(FlagContext::getContestId, contestId)
                .eq(FlagContext::getTeamId, teamId)
                .eq(FlagContext::getQuestionId, questionId)
                .last("LIMIT 1"));

        if (fc != null) return fc.getFlag();

        CtfQuestion stub = new CtfQuestion();
        stub.setId(questionId);
        String flag = dynamicFlagValidator.expectedFlag(JudgeContext.builder()
                .contestId(contestId).teamId(teamId).question(stub).build());

        FlagContext newFc = new FlagContext();
        newFc.setContestId(contestId);
        newFc.setTeamId(teamId);
        newFc.setQuestionId(questionId);
        newFc.setFlag(flag);
        newFc.setFlagType("DYNAMIC");
        newFc.setIsCorrect(null);
        newFc.setCreatedAt(LocalDateTime.now());
        newFc.setUpdatedAt(LocalDateTime.now());
        flagContextMapper.insert(newFc);

        return flag;
    }

    private ChallengeContainer findRunningContainer(Long contestId, Long teamId, Long questionId) {
        return containerMapper.selectOne(new LambdaQueryWrapper<ChallengeContainer>()
                .eq(ChallengeContainer::getContestId, contestId)
                .eq(ChallengeContainer::getTeamId, teamId)
                .eq(ChallengeContainer::getQuestionId, questionId)
                .eq(ChallengeContainer::getStatus, "RUNNING")
                .last("LIMIT 1"));
    }

    private ChallengeContainer findContainer(Long contestId, Long teamId, Long questionId) {
        return containerMapper.selectOne(new LambdaQueryWrapper<ChallengeContainer>()
                .eq(ChallengeContainer::getContestId, contestId)
                .eq(ChallengeContainer::getTeamId, teamId)
                .eq(ChallengeContainer::getQuestionId, questionId)
                .ne(ChallengeContainer::getStatus, "DESTROYED")
                .last("LIMIT 1"));
    }

    private String buildContainerName(Long contestId, Long teamId, Long questionId) {
        return "csp-c" + contestId + "-t" + teamId + "-q" + questionId;
    }
}
