package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.entity.ContestInfo;
import com.csp.platform.entity.Question;
import com.csp.platform.entity.Writeup;
import com.csp.platform.mapper.ContestInfoMapper;
import com.csp.platform.mapper.QuestionMapper;
import com.csp.platform.mapper.WriteupMapper;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 题解（Writeup）：提交、审核、公开浏览、点赞。 */
@Service
@RequiredArgsConstructor
public class WriteupService {

    private final WriteupMapper writeupMapper;
    private final QuestionMapper questionMapper;
    private final ContestInfoMapper contestInfoMapper;

    /** 提交题解（进入待审核）。 */
    @Transactional
    public Writeup submit(Writeup cmd) {
        Long uid = UserContext.requireUserId();
        if (cmd.getTitle() == null || cmd.getTitle().isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "题解标题不能为空");
        }
        if (cmd.getQuestionId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "题目ID不能为空");
        }
        if (cmd.getContent() == null || cmd.getContent().isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "题解内容不能为空");
        }
        Writeup w = new Writeup();
        w.setContestId(cmd.getContestId());
        w.setQuestionId(cmd.getQuestionId());
        w.setTeamId(cmd.getTeamId());
        w.setUserId(uid);
        w.setTitle(cmd.getTitle());
        w.setContent(cmd.getContent());
        w.setStatus("PENDING");
        w.setViewCount(0);
        w.setLikeCount(0);
        writeupMapper.insert(w);
        return w;
    }

    /** 公开列表：仅 APPROVED；可按题目/赛事过滤。 */
    public PageResult<Writeup> publicPage(Integer page, Integer size, Long questionId, Long contestId, String keyword) {
        LambdaQueryWrapper<Writeup> w = new LambdaQueryWrapper<Writeup>()
                .eq(Writeup::getStatus, "APPROVED");
        if (questionId != null) w.eq(Writeup::getQuestionId, questionId);
        if (contestId != null) w.eq(Writeup::getContestId, contestId);
        if (keyword != null && !keyword.isBlank()) w.like(Writeup::getTitle, keyword);
        w.orderByDesc(Writeup::getCreatedAt);
        Page<Writeup> p = writeupMapper.selectPage(new Page<>(page, size), w);
        return PageResult.of(p);
    }

    /** 我的题解（含待审核/被拒）。 */
    public List<Writeup> mine() {
        Long uid = UserContext.requireUserId();
        return writeupMapper.selectList(new LambdaQueryWrapper<Writeup>()
                .eq(Writeup::getUserId, uid)
                .orderByDesc(Writeup::getCreatedAt));
    }

    /** 待审核列表（管理端）。 */
    public PageResult<Writeup> reviewPage(Integer page, Integer size, String status) {
        LambdaQueryWrapper<Writeup> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Writeup::getStatus, status);
        w.orderByAsc(Writeup::getCreatedAt);
        Page<Writeup> p = writeupMapper.selectPage(new Page<>(page, size), w);
        fillNames(p.getRecords());
        return PageResult.of(p);
    }

    /** 批量补全题目名、赛事名，避免前端展示裸 ID。 */
    private void fillNames(List<Writeup> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> qids = list.stream().map(Writeup::getQuestionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> cids = list.stream().map(Writeup::getContestId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> qMap = qids.isEmpty() ? Collections.emptyMap()
                : questionMapper.selectBatchIds(qids).stream()
                    .collect(Collectors.toMap(Question::getId, Question::getTitle, (a, b) -> a));
        Map<Long, String> cMap = cids.isEmpty() ? Collections.emptyMap()
                : contestInfoMapper.selectBatchIds(cids).stream()
                    .collect(Collectors.toMap(ContestInfo::getId, ContestInfo::getName, (a, b) -> a));
        for (Writeup w : list) {
            w.setQuestionTitle(qMap.get(w.getQuestionId()));
            w.setContestName(cMap.get(w.getContestId()));
        }
    }

    /** 审核：通过 / 拒绝。 */
    @Transactional
    public Writeup review(Long id, String status, String remark) {
        Writeup w = writeupMapper.selectById(id);
        if (w == null) throw new BizException(ResultCode.NOT_FOUND, "题解不存在");
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!s.equals("APPROVED") && !s.equals("REJECTED")) {
            throw new BizException(ResultCode.BAD_REQUEST, "审核状态只能是 APPROVED 或 REJECTED");
        }
        w.setStatus(s);
        w.setReviewRemark(remark);
        w.setReviewedBy(UserContext.requireUserId());
        w.setReviewedAt(LocalDateTime.now());
        writeupMapper.updateById(w);
        return w;
    }

    /** 详情：APPROVED 对所有人可见；作者本人可见自己的；审核人可见全部。 */
    public Writeup detail(Long id) {
        Writeup w = writeupMapper.selectById(id);
        if (w == null) throw new BizException(ResultCode.NOT_FOUND, "题解不存在");
        Long uid = UserContext.get() == null ? null : UserContext.get().getUserId();
        boolean isAuthor = uid != null && uid.equals(w.getUserId());
        if (!"APPROVED".equals(w.getStatus()) && !isAuthor) {
            throw new BizException(ResultCode.NOT_FOUND, "题解不存在或未公开");
        }
        fillNames(List.of(w));
        // 浏览量 +1
        writeupMapper.update(null, new LambdaUpdateWrapper<Writeup>()
                .eq(Writeup::getId, id).setSql("view_count = view_count + 1"));
        w.setViewCount((w.getViewCount() == null ? 0 : w.getViewCount()) + 1);
        return w;
    }

    /** 点赞。 */
    @Transactional
    public int like(Long id) {
        Writeup w = writeupMapper.selectById(id);
        if (w == null) throw new BizException(ResultCode.NOT_FOUND, "题解不存在");
        if (!"APPROVED".equals(w.getStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅已公开题解可点赞");
        }
        writeupMapper.update(null, new LambdaUpdateWrapper<Writeup>()
                .eq(Writeup::getId, id).setSql("like_count = like_count + 1"));
        return (w.getLikeCount() == null ? 0 : w.getLikeCount()) + 1;
    }

    /** 删除：作者本人或管理员。 */
    @Transactional
    public void delete(Long id) {
        Writeup w = writeupMapper.selectById(id);
        if (w == null) throw new BizException(ResultCode.NOT_FOUND, "题解不存在");
        Long uid = UserContext.requireUserId();
        if (!uid.equals(w.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "只能删除自己的题解");
        }
        writeupMapper.deleteById(id);
    }
}
