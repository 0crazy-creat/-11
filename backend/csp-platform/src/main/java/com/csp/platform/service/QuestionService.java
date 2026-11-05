package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.QuestionQuery;
import com.csp.platform.dto.QuestionSaveCmd;
import com.csp.platform.entity.Question;
import com.csp.platform.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 统一题库服务：理论 / CTF / 场景 / AWD 共用。 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;

    public PageResult<Question> page(QuestionQuery q) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getBankType())) w.eq(Question::getBankType, q.getBankType());
        // 题库里存在 WEB/Web、PWN/Pwn 等历史混用，按小写比对避免筛选漏题
        if (StringUtils.hasText(q.getCategory())) w.apply("LOWER(category) = LOWER({0})", q.getCategory());
        if (StringUtils.hasText(q.getDifficulty())) w.eq(Question::getDifficulty, q.getDifficulty());
        if (q.getStatus() != null) w.eq(Question::getStatus, q.getStatus());
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(Question::getTitle, kw).or().like(Question::getContent, kw));
        }
        w.orderByDesc(Question::getCreatedAt);
        Page<Question> p = questionMapper.selectPage(new Page<>(q.getPage(), q.getSize()), w);
        return PageResult.of(p);
    }

    public Question get(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BizException(ResultCode.NOT_FOUND, "题目不存在：" + id);
        return q;
    }

    @Transactional
    public Question create(QuestionSaveCmd cmd) {
        Question q = new Question();
        BeanUtils.copyProperties(cmd, q);
        validateFlag(cmd);
        questionMapper.insert(q);
        return q;
    }

    @Transactional
    public Question update(Long id, QuestionSaveCmd cmd) {
        Question exist = get(id);
        BeanUtils.copyProperties(cmd, exist, "id", "createdBy", "createdAt", "deleted");
        validateFlag(cmd);
        questionMapper.updateById(exist);
        return exist;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        questionMapper.deleteById(id);
    }

    /** 按 id 集合批量查询（供赛事组卷使用）。 */
    public List<Question> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return questionMapper.selectBatchIds(ids);
    }

    /** 批量导入（同一事务，任一校验失败整体回滚）。 */
    @Transactional
    public int importBatch(List<QuestionSaveCmd> cmds) {
        if (cmds == null || cmds.isEmpty()) return 0;
        int n = 0;
        for (QuestionSaveCmd cmd : cmds) {
            Question q = new Question();
            BeanUtils.copyProperties(cmd, q);
            validateFlag(cmd);
            questionMapper.insert(q);
            n++;
        }
        return n;
    }

    private void validateFlag(QuestionSaveCmd cmd) {
        if ("CTF".equals(cmd.getBankType())
                && !StringUtils.hasText(cmd.getFlag())
                && !StringUtils.hasText(cmd.getImageName())) {
            throw new BizException(ResultCode.BAD_REQUEST, "CTF 题目必须提供静态 flag 或靶场镜像");
        }
    }
}
