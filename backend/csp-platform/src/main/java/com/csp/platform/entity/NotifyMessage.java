package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 站内信 / 通知。userId 为空表示赛事级广播。 */
@Data
@TableName("notify_message")
public class NotifyMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    /** 为空表示广播 */
    private Long userId;
    /** INBOX/WS/EMAIL/SMS */
    private String channel;
    private String title;
    private String content;
    /** 0 未读 1 已读 */
    private Integer readFlag;
    private LocalDateTime sentAt;
    /** 投递状态：delivered / pending:原因（EMAIL/SMS 渠道） */
    private String deliverStatus;
}
