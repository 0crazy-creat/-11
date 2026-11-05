package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.NotifyQuery;
import com.csp.platform.dto.NotifySendCmd;
import com.csp.platform.entity.NotifyMessage;
import com.csp.platform.mapper.NotifyMessageMapper;
import com.csp.platform.notify.NotifyChannel;
import com.csp.platform.notify.NotifyDispatcher;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 通知服务：站内信 + 广播，已读状态按用户维度处理。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final NotifyMessageMapper notifyMapper;
    private final NotifyDispatcher notifyDispatcher;

    /** 发送通知：userId 为空表示赛事广播。 */
    @Transactional
    public NotifyMessage send(NotifySendCmd cmd) {
        NotifyMessage m = new NotifyMessage();
        m.setContestId(cmd.getContestId());
        m.setUserId(cmd.getUserId());
        m.setChannel(cmd.getChannel() == null ? "INBOX" : cmd.getChannel());
        m.setTitle(cmd.getTitle());
        m.setContent(cmd.getContent());
        m.setReadFlag(0);
        m.setSentAt(LocalDateTime.now());
        notifyMapper.insert(m);
        // 真实投递：EMAIL 走 SMTP，SMS 走网关；INBOX 仅落库。失败不回滚站内信。
        NotifyChannel ch = NotifyChannel.of(m.getChannel());
        NotifyDispatcher.Result r = notifyDispatcher.dispatch(
                ch, m.getUserId(), m.getTitle(), m.getContent());
        m.setDeliverStatus(r.delivered() ? "delivered" : "pending:" + r.detail());
        log.info("通知已发送 id={} contestId={} userId={} channel={} delivered={} detail={}",
                m.getId(), m.getContestId(), m.getUserId(), m.getChannel(), r.delivered(), r.detail());
        return m;
    }

    /** 当前用户可见通知：自己的 + 广播（可选过滤赛事）。 */
    public PageResult<NotifyMessage> myPage(NotifyQuery q) {
        Long uid = UserContext.requireUserId();
        LambdaQueryWrapper<NotifyMessage> w = new LambdaQueryWrapper<>();
        if (Boolean.FALSE.equals(q.getIncludeBroadcast())) {
            w.eq(NotifyMessage::getUserId, uid);
        } else {
            w.and(x -> x.eq(NotifyMessage::getUserId, uid).or().isNull(NotifyMessage::getUserId));
        }
        if (q.getContestId() != null) w.eq(NotifyMessage::getContestId, q.getContestId());
        if (q.getReadFlag() != null) w.eq(NotifyMessage::getReadFlag, q.getReadFlag());
        w.orderByDesc(NotifyMessage::getSentAt);
        Page<NotifyMessage> p = notifyMapper.selectPage(new Page<>(q.getPage(), q.getSize()), w);
        return PageResult.of(p);
    }

    /** 统计当前用户未读数（含广播）。 */
    public long unreadCount() {
        Long uid = UserContext.requireUserId();
        return notifyMapper.selectCount(new LambdaQueryWrapper<NotifyMessage>()
                .eq(NotifyMessage::getReadFlag, 0)
                .and(x -> x.eq(NotifyMessage::getUserId, uid).or().isNull(NotifyMessage::getUserId)));
    }

    /** 批量标记已读：自己的 + 广播（广播 userId 为 null，必须一并匹配，否则永远无法置读）。 */
    @Transactional
    public int markRead(List<Long> ids) {
        Long uid = UserContext.requireUserId();
        if (ids == null || ids.isEmpty()) return 0;
        return notifyMapper.update(null, new LambdaUpdateWrapper<NotifyMessage>()
                .set(NotifyMessage::getReadFlag, 1)
                .in(NotifyMessage::getId, ids)
                .and(x -> x.eq(NotifyMessage::getUserId, uid).or().isNull(NotifyMessage::getUserId)));
    }

    /** 全部标记已读：自己的 + 广播。 */
    @Transactional
    public int markAllRead() {
        Long uid = UserContext.requireUserId();
        return notifyMapper.update(null, new LambdaUpdateWrapper<NotifyMessage>()
                .set(NotifyMessage::getReadFlag, 1)
                .eq(NotifyMessage::getReadFlag, 0)
                .and(x -> x.eq(NotifyMessage::getUserId, uid).or().isNull(NotifyMessage::getUserId)));
    }

    public NotifyMessage get(Long id) {
        NotifyMessage m = notifyMapper.selectById(id);
        if (m == null) throw new BizException(ResultCode.NOT_FOUND, "通知不存在：" + id);
        Long uid = UserContext.requireUserId();
        if (m.getUserId() != null && !m.getUserId().equals(uid) && !UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "无权查看该通知");
        }
        return m;
    }
}
