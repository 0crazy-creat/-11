package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.ArchiveCmd;
import com.csp.platform.dto.ArchiveVO;
import com.csp.platform.dto.ContestQuestionVO;
import com.csp.platform.entity.*;
import com.csp.platform.enums.ContestStatus;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.entity.ContestTeam;
import com.csp.platform.mapper.ContestArchiveMapper;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.platform.mapper.SubmissionStatMapper;
import com.csp.security.core.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛事备案归档：生成不可变快照（含题目清单、战队、成绩统计与完整性校验和），
 * 并把赛事推进到 ARCHIVED 状态。归档记录只追加，不允许更新或删除。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ContestArchiveMapper archiveMapper;
    private final ContestQuestionMapper contestQuestionMapper;
    private final ContestTeamMapper teamMapper;
    private final SubmissionStatMapper statMapper;
    private final ContestService contestService;
    private final NotifyService notifyService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    /** 生成归档快照并流转赛事状态到 ARCHIVED。 */
    @Transactional
    public ArchiveVO archive(Long contestId, ArchiveCmd cmd) {
        ContestInfo c = contestService.get(contestId);
        ContestStatus cur = ContestStatus.of(c.getStatus());
        if (cur != ContestStatus.FINISHED && cur != ContestStatus.ARCHIVED) {
            throw new BizException(ResultCode.CONFLICT,
                    "仅已结束(FINISHED)的赛事可归档，当前状态：" + cur);
        }
        Long dup = archiveMapper.selectCount(new LambdaQueryWrapper<ContestArchive>()
                .eq(ContestArchive::getContestId, contestId));
        if (dup != null && dup > 0) {
            throw new BizException(ResultCode.CONFLICT, "该赛事已存在归档快照，归档不可重复");
        }

        List<ContestQuestionVO> questions = contestService.listQuestions(contestId, true);
        List<ContestTeam> teams = teamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId)
                .orderByDesc(ContestTeam::getScore));
        long subCount = statMapper.countSubmissions(contestId);
        long playerCount = statMapper.countPlayers(contestId);

        // 快照内容：赛事主体 + 题目 + 战队榜单 + 统计
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contest", c);
        snapshot.put("questions", questions);
        snapshot.put("teams", teams);
        snapshot.put("stats", Map.of(
                "questionCount", questions.size(),
                "teamCount", teams.size(),
                "submissionCount", subCount,
                "playerCount", playerCount));
        String json;
        try {
            json = objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "归档快照序列化失败：" + e.getMessage());
        }
        String checksum = sha256(json);

        Long uid = UserContext.requireUserId();
        ContestArchive a = new ContestArchive();
        a.setContestId(contestId);
        a.setContestCode(c.getCode());
        a.setContestName(c.getName());
        a.setContestType(c.getType());
        a.setContestStatus(c.getStatus());
        a.setFilingNo(cmd.getFilingNo());
        a.setFilingOrg(cmd.getFilingOrg());
        a.setRemark(cmd.getRemark());
        a.setQuestionCount((long) questions.size());
        a.setTeamCount((long) teams.size());
        a.setSubmissionCount(subCount);
        a.setPlayerCount(playerCount);
        a.setChecksum(checksum);
        a.setSnapshot(json);
        a.setArchivedAt(LocalDateTime.now());
        a.setArchivedBy(uid);
        archiveMapper.insert(a);

        // 状态推进到 ARCHIVED（FINISHED → ARCHIVED）
        if (cur == ContestStatus.FINISHED) {
            c.setStatus(ContestStatus.ARCHIVED.name());
            c.setUpdatedBy(uid);
            c.setUpdatedAt(LocalDateTime.now());
            // 复用 contestService 的状态机校验
            com.csp.platform.dto.ContestStatusCmd sc = new com.csp.platform.dto.ContestStatusCmd();
            sc.setStatus(ContestStatus.ARCHIVED.name());
            contestService.changeStatus(contestId, sc);
        }

        // 归档完成通知赛事组织者（广播到赛事维度）
        com.csp.platform.dto.NotifySendCmd n = new com.csp.platform.dto.NotifySendCmd();
        n.setContestId(contestId);
        n.setChannel("INBOX");
        n.setTitle("赛事已归档备案");
        n.setContent("赛事【" + c.getName() + "】已完成归档，备案编号：" + cmd.getFilingNo()
                + "，校验和：" + checksum);
        notifyService.send(n);

        auditService.success("CONTEST_ARCHIVE", "contest:" + contestId,
                "filingNo=" + cmd.getFilingNo() + ",checksum=" + checksum);

        ArchiveVO vo = toVO(a);
        log.info("赛事归档完成 contestId={} filingNo={} checksum={}", contestId, cmd.getFilingNo(), checksum);
        return vo;
    }

    /** 校验归档快照完整性：重新计算 SHA-256 与存档值比对。 */
    public boolean verify(Long archiveId) {
        ContestArchive a = get(archiveId);
        if (a.getSnapshot() == null) return false;
        return sha256(a.getSnapshot()).equals(a.getChecksum());
    }

    public ContestArchive get(Long archiveId) {
        ContestArchive a = archiveMapper.selectById(archiveId);
        if (a == null) throw new BizException(ResultCode.NOT_FOUND, "归档记录不存在：" + archiveId);
        return a;
    }

    public ArchiveVO detail(Long archiveId) {
        return toVO(get(archiveId));
    }

    public ContestArchive getByContest(Long contestId) {
        ContestArchive a = archiveMapper.selectOne(new LambdaQueryWrapper<ContestArchive>()
                .eq(ContestArchive::getContestId, contestId)
                .orderByDesc(ContestArchive::getArchivedAt)
                .last("LIMIT 1"));
        if (a == null) throw new BizException(ResultCode.NOT_FOUND, "赛事尚未归档：" + contestId);
        return a;
    }

    public PageResult<ContestArchive> page(long page, long size, String filingNo, Long contestId) {
        LambdaQueryWrapper<ContestArchive> w = new LambdaQueryWrapper<>();
        if (filingNo != null && !filingNo.isBlank()) w.like(ContestArchive::getFilingNo, filingNo);
        if (contestId != null) w.eq(ContestArchive::getContestId, contestId);
        w.orderByDesc(ContestArchive::getArchivedAt);
        Page<ContestArchive> p = archiveMapper.selectPage(new Page<>(page, size), w);
        return PageResult.of(p);
    }

    private ArchiveVO toVO(ContestArchive a) {
        ArchiveVO v = new ArchiveVO();
        v.setContestId(a.getContestId());
        v.setContestCode(a.getContestCode());
        v.setContestName(a.getContestName());
        v.setContestType(a.getContestType());
        v.setStatus(a.getContestStatus());
        v.setFilingNo(a.getFilingNo());
        v.setFilingOrg(a.getFilingOrg());
        v.setRemark(a.getRemark());
        v.setQuestionCount(a.getQuestionCount());
        v.setTeamCount(a.getTeamCount());
        v.setSubmissionCount(a.getSubmissionCount());
        v.setPlayerCount(a.getPlayerCount());
        v.setChecksum(a.getChecksum());
        v.setArchivedAt(a.getArchivedAt());
        v.setArchivedBy(a.getArchivedBy());
        return v;
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
