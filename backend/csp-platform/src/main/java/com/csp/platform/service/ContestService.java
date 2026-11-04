package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.*;
import com.csp.platform.entity.ContestInfo;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.platform.entity.Question;
import com.csp.platform.enums.ContestStatus;
import com.csp.platform.format.ContestFormatHandler;
import com.csp.platform.format.ContestFormatRouter;
import com.csp.platform.mapper.ContestInfoMapper;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 赛事管理服务：CRUD、状态机流转、组卷。 */
@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestInfoMapper contestMapper;
    private final ContestQuestionMapper contestQuestionMapper;
    private final QuestionService questionService;
    private final ContestFormatRouter formatRouter;

    public PageResult<ContestInfo> page(ContestQuery q) {
        LambdaQueryWrapper<ContestInfo> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getStatus())) w.eq(ContestInfo::getStatus, q.getStatus().toUpperCase());
        if (StringUtils.hasText(q.getType())) w.eq(ContestInfo::getType, q.getType().toUpperCase());
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(ContestInfo::getName, kw).or().like(ContestInfo::getCode, kw));
        }
        w.orderByDesc(ContestInfo::getCreatedAt);
        Page<ContestInfo> p = contestMapper.selectPage(new Page<>(q.getPage(), q.getSize()), w);
        return PageResult.of(p);
    }

    public ContestInfo get(Long id) {
        ContestInfo c = contestMapper.selectById(id);
        if (c == null) throw new BizException(ResultCode.NOT_FOUND, "赛事不存在：" + id);
        return c;
    }

    public ContestInfo getByCode(String code) {
        ContestInfo c = contestMapper.selectOne(new LambdaQueryWrapper<ContestInfo>()
                .eq(ContestInfo::getCode, code));
        if (c == null) throw new BizException(ResultCode.NOT_FOUND, "赛事不存在：" + code);
        return c;
    }

    @Transactional
    public ContestInfo create(ContestSaveCmd cmd) {
        Long dup = contestMapper.selectCount(new LambdaQueryWrapper<ContestInfo>()
                .eq(ContestInfo::getCode, cmd.getCode()));
        if (dup != null && dup > 0) throw new BizException(ResultCode.CONFLICT, "赛事编码已存在：" + cmd.getCode());

        // 按赛制路由：未知 type 直接拒绝（策略层校验，避免后续运行期走错分支）
        ContestFormatHandler handler = formatRouter.of(cmd.getType());

        ContestInfo c = new ContestInfo();
        BeanUtils.copyProperties(cmd, c);
        // 赛制编码以处理器声明为准，归一化为大写标准码
        c.setType(handler.type());
        c.setStatus(ContestStatus.DRAFT.name());
        // 默认独立 schema，保证比赛粒度数据隔离
        if (!StringUtils.hasText(c.getSchemaName())) {
            c.setSchemaName("csp_" + cmd.getCode().toLowerCase().replace('-', '_'));
        }
        contestMapper.insert(c);
        return c;
    }

    @Transactional
    public ContestInfo update(Long id, ContestSaveCmd cmd) {
        ContestInfo exist = get(id);
        if (ContestStatus.RUNNING.name().equals(exist.getStatus())
                || ContestStatus.FINISHED.name().equals(exist.getStatus())
                || ContestStatus.ARCHIVED.name().equals(exist.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "赛事 " + exist.getStatus() + " 状态下不允许编辑基础信息");
        }
        if (!exist.getCode().equals(cmd.getCode())) {
            Long dup = contestMapper.selectCount(new LambdaQueryWrapper<ContestInfo>()
                    .eq(ContestInfo::getCode, cmd.getCode()).ne(ContestInfo::getId, id));
            if (dup != null && dup > 0) throw new BizException(ResultCode.CONFLICT, "赛事编码已存在：" + cmd.getCode());
        }
        BeanUtils.copyProperties(cmd, exist, "id", "status", "createdBy", "createdAt", "deleted");
        contestMapper.updateById(exist);
        return exist;
    }

    @Transactional
    public void delete(Long id) {
        ContestInfo exist = get(id);
        if (ContestStatus.RUNNING.name().equals(exist.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "运行中的赛事不可删除，请先结束");
        }
        contestMapper.deleteById(id);
    }

    /** 状态机流转，非法跃迁直接拒绝。 */
    @Transactional
    public ContestInfo changeStatus(Long id, ContestStatusCmd cmd) {
        ContestInfo exist = get(id);
        ContestStatus from = ContestStatus.of(exist.getStatus());
        ContestStatus to;
        try {
            to = ContestStatus.of(cmd.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BizException(ResultCode.BAD_REQUEST, "未知目标状态：" + cmd.getStatus());
        }
        if (from == to) return exist;
        if (!from.canTransitTo(to)) {
            throw new BizException(ResultCode.CONFLICT,
                    "非法状态流转：" + from + " → " + to + "（允许：" + Arrays.toString(
                            Arrays.stream(ContestStatus.values())
                                    .filter(from::canTransitTo).toArray()) + "）");
        }
        exist.setStatus(to.name());
        contestMapper.updateById(exist);
        ContestFormatHandler handler = formatRouter.of(exist);
        if (to == ContestStatus.RUNNING) {
            handler.onStart(exist);
        } else if (to == ContestStatus.FINISHED) {
            handler.onFinish(exist);
        }
        return exist;
    }

    // ---------- 组卷 ----------

    @Transactional
    public void bindQuestions(Long contestId, ContestQuestionBindCmd cmd) {
        ContestInfo c = get(contestId);
        if (ContestStatus.FINISHED.name().equals(c.getStatus()) || ContestStatus.ARCHIVED.name().equals(c.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "已结束赛事不可再组卷");
        }
        List<Long> ids = cmd.getQuestionIds().stream().distinct().toList();
        List<Question> questions = questionService.listByIds(ids);
        if (questions.size() != ids.size()) {
            Set<Long> found = questions.stream().map(Question::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(i -> !found.contains(i)).toList();
            throw new BizException(ResultCode.BAD_REQUEST, "题目不存在：" + missing);
        }
        Set<String> bankTypes = questions.stream().map(Question::getBankType).collect(Collectors.toSet());
        if (bankTypes.size() > 1 && !formatRouter.of(c).allowCrossBankTypes()) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "单场赛事组卷题库类型需一致，检测到：" + bankTypes);
        }

        if (cmd.isOverwrite()) {
            contestQuestionMapper.delete(new LambdaQueryWrapper<ContestQuestion>()
                    .eq(ContestQuestion::getContestId, contestId));
        }
        // 追加模式下从当前最大排序号继续
        int sort = cmd.isOverwrite() ? 0 : currentMaxSort(contestId);
        Set<Long> already = cmd.isOverwrite() ? Set.of() : currentQuestionIds(contestId);

        for (Long qid : ids) {
            if (already.contains(qid)) continue;
            ContestQuestion cq = new ContestQuestion();
            cq.setContestId(contestId);
            cq.setQuestionId(qid);
            cq.setSortNo(++sort);
            cq.setVisible(1);
            contestQuestionMapper.insert(cq);
        }
    }

    public List<ContestQuestionVO> listQuestions(Long contestId, boolean includeHidden) {
        get(contestId);
        List<ContestQuestion> binds = contestQuestionMapper.selectList(
                new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, contestId)
                        .eq(!includeHidden, ContestQuestion::getVisible, 1)
                        .orderByAsc(ContestQuestion::getSortNo));
        if (binds.isEmpty()) return List.of();
        Map<Long, Question> qmap = questionService.listByIds(
                        binds.stream().map(ContestQuestion::getQuestionId).toList())
                .stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        return binds.stream().map(b -> ContestQuestionVO.of(b, qmap.get(b.getQuestionId()))).toList();
    }

    @Transactional
    public void unbindQuestion(Long contestId, Long questionId) {
        get(contestId);
        contestQuestionMapper.delete(new LambdaQueryWrapper<ContestQuestion>()
                .eq(ContestQuestion::getContestId, contestId)
                .eq(ContestQuestion::getQuestionId, questionId));
    }

    private int currentMaxSort(Long contestId) {
        List<ContestQuestion> l = contestQuestionMapper.selectList(new LambdaQueryWrapper<ContestQuestion>()
                .eq(ContestQuestion::getContestId, contestId)
                .orderByDesc(ContestQuestion::getSortNo).last("LIMIT 1"));
        return l.isEmpty() || l.get(0).getSortNo() == null ? 0 : l.get(0).getSortNo();
    }

    private Set<Long> currentQuestionIds(Long contestId) {
        return contestQuestionMapper.selectList(new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, contestId))
                .stream().map(ContestQuestion::getQuestionId).collect(Collectors.toSet());
    }
}
