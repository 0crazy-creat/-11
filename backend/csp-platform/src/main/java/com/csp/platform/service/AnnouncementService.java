package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.entity.Announcement;
import com.csp.infra.ws.WsPublisher;
import com.csp.platform.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/** 平台公告服务：全平台公告 + 赛事公告。 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final WsPublisher wsPublisher;

    public PageResult<Announcement> page(Integer page, Integer size, String scope, Long contestId, String keyword) {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(scope)) w.eq(Announcement::getScope, scope);
        if (contestId != null) w.eq(Announcement::getContestId, contestId);
        if (StringUtils.hasText(keyword)) w.like(Announcement::getTitle, keyword.trim());
        w.orderByDesc(Announcement::getCreatedAt);
        Page<Announcement> p = announcementMapper.selectPage(
                new Page<>(page == null ? 1 : page, size == null ? 10 : size), w);
        return PageResult.of(p);
    }

    public Announcement get(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BizException(ResultCode.NOT_FOUND, "公告不存在：" + id);
        return a;
    }

    @Transactional
    public Announcement create(Announcement cmd) {
        Announcement a = new Announcement();
        a.setTitle(cmd.getTitle());
        a.setContent(cmd.getContent());
        a.setScope(StringUtils.hasText(cmd.getScope()) ? cmd.getScope() : "GLOBAL");
        a.setContestId(cmd.getContestId());
        a.setStatus(StringUtils.hasText(cmd.getStatus()) ? cmd.getStatus() : "PUBLISHED");
        announcementMapper.insert(a);
        // 实时推送到该赛事房间（scope=GLOBAL 时 contestId 可能为空，跳过）
        if (a.getContestId() != null && "PUBLISHED".equals(a.getStatus())) {
            wsPublisher.announce(a.getContestId(), java.util.Map.of(
                    "id", a.getId(), "title", a.getTitle(), "content", a.getContent()));
        }
        return a;
    }

    @Transactional
    public Announcement update(Long id, Announcement cmd) {
        Announcement a = get(id);
        if (StringUtils.hasText(cmd.getTitle())) a.setTitle(cmd.getTitle());
        if (cmd.getContent() != null) a.setContent(cmd.getContent());
        if (StringUtils.hasText(cmd.getScope())) a.setScope(cmd.getScope());
        if (cmd.getContestId() != null) a.setContestId(cmd.getContestId());
        if (StringUtils.hasText(cmd.getStatus())) a.setStatus(cmd.getStatus());
        announcementMapper.updateById(a);
        return a;
    }

    @Transactional
    public void remove(Long id) {
        get(id);
        announcementMapper.deleteById(id);
    }

    /** 首页展示：最近 N 条已发布公告。 */
    public java.util.List<Announcement> latest(int n) {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.eq(Announcement::getStatus, "PUBLISHED")
                .orderByDesc(Announcement::getCreatedAt)
                .last("LIMIT " + Math.max(1, Math.min(n, 50)));
        return announcementMapper.selectList(w);
    }
}