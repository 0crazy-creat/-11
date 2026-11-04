package com.csp.cfs.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** CFS flag 提交结果：含层解锁变化与递进得分。 */
@Data
@Builder
public class CfsSubmitResult {
    private Boolean correct;
    private Integer score;
    private String message;
    /** 本次提交命中的层号 */
    private Integer layerNo;
    /** 提交前已解锁的最高层 */
    private Integer unlockedBefore;
    /** 提交后已解锁的最高层 */
    private Integer unlockedAfter;
    /** 本次是否推进了解锁 */
    private Boolean layerUnlocked;
    /** 本次新解锁的层号（如 2），无则为 null */
    private Integer newlyUnlockedLayer;
    /** 当前累计总分 */
    private Long totalScore;
    /** 已解出层号列表 */
    private List<Integer> solvedLayers;
    private Long elapsedMs;
}