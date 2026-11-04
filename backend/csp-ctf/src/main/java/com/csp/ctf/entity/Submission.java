package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 提交记录（submission）。注意该表无逻辑删除/审计字段。 */
@Data
@TableName("submission")
public class Submission implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long userId;
    private Long questionId;
    /** FLAG/ANSWER/ATTACK/DEFENSE/EVIDENCE */
    private String submitType;
    /** 归一化后的提交内容（脱敏） */
    private String content;
    private Integer correct;
    private Integer score;
    private Integer roundNo;
    private String ip;
    private String deviceId;
    private Long elapsedMs;
    private LocalDateTime createdAt;
}
