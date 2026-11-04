package com.csp.awd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 单台靶机一次 check 的结果。
 *
 * <p>status 取值：
 * <ul>
 *   <li>{@code PASS} —— 探活 + 业务功能校验均通过</li>
 *   <li>{@code FAIL} —— 可达但功能校验失败（如 /health 返回 5xx）</li>
 *   <li>{@code TIMEOUT} —— 连接/读取超时</li>
 *   <li>{@code UNREACHABLE} —— 连接被拒/无可用地址/网络错误</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckResultVO {

    /** 赛事 ID。 */
    private Long contestId;

    /** 轮次号（从 1 开始）。 */
    private Integer roundNo;

    /** 战队 ID。 */
    private Long teamId;

    /** 题目 ID。 */
    private Long questionId;

    /** PASS / FAIL / TIMEOUT / UNREACHABLE。 */
    private String status;

    /** 最后一次探测的 HTTP 状态码（0 表示未拿到响应）。 */
    private Integer httpCode;

    /** 该靶机 check 总耗时（毫秒）。 */
    private Long latencyMs;

    /** 失败原因（PASS 时为 null）。 */
    private String reason;

    /** 该靶机对应的 SLA 扣分值（负数或 0，由结算层填充）。 */
    private Integer slaDelta;

    /** 检查时间。 */
    private LocalDateTime checkedAt;

    /** 是否通过。 */
    public boolean passed() {
        return "PASS".equals(status);
    }
}