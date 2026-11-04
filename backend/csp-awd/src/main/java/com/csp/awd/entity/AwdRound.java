package com.csp.awd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** AWD 轮次。 */
@Data
@TableName("awd_round")
public class AwdRound {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Integer roundNo;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer durationS;
}
