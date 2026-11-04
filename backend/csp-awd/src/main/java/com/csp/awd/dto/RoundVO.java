package com.csp.awd.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** AWD 轮次视图。 */
@Data
@Builder
public class RoundVO {
    private Long id;
    private Long contestId;
    private Integer roundNo;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer durationS;
    /** 剩余秒数（RUNNING 时有值，否则 null） */
    private Long remainingSeconds;
}