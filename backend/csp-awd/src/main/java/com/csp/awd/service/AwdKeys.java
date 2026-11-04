package com.csp.awd.service;

/**
 * AWD 子系统 Redis Key 规范（沿用 csp: 前缀，模块内集中管理）。
 *
 * <p>排行榜类均为 ZSet，member 为 teamId，score 为分值。</p>
 */
public final class AwdKeys {
    private static final String P = "csp:awd:";
    private AwdKeys() {}

    /** 总分榜：csp:awd:total:{contestId} */
    public static String total(Long contestId) { return P + "total:" + contestId; }
    /** 攻击分榜 */
    public static String attack(Long contestId) { return P + "attack:" + contestId; }
    /** 防御分榜 */
    public static String defense(Long contestId) { return P + "defense:" + contestId; }
    /** SLA 扣分（负值累计） */
    public static String sla(Long contestId) { return P + "sla:" + contestId; }
    /** 被攻破次数（累计） */
    public static String compromised(Long contestId) { return P + "compromised:" + contestId; }
    /** 被攻破次数（按轮次） */
    public static String compromisedRound(Long contestId, int roundNo) {
        return P + "compromised:r" + roundNo + ":" + contestId;
    }
    /** 攻击成功次数（累计，按队） */
    public static String attackHits(Long contestId) { return P + "attack:hits:" + contestId; }

    /** 攻破事实：csp:awd:hit:{contestId}:r{round}:{attacker}:{victim}:{qid}，值为次数 */
    public static String hit(Long contestId, int roundNo, Long attacker, Long victim, Long questionId) {
        return P + "hit:" + contestId + ":r" + roundNo + ":" + attacker + ":" + victim + ":" + questionId;
    }
    /** 某轮 hit key 扫描前缀（运维排查用） */
    public static String hitScanPrefix(Long contestId, int roundNo) {
        return P + "hit:" + contestId + ":r" + roundNo + ":";
    }

    /** 首杀：csp:awd:firstblood:{contestId}:r{round}:{victim}:{qid}，值为首杀队ID */
    public static String firstBlood(Long contestId, int roundNo, Long victim, Long questionId) {
        return P + "firstblood:" + contestId + ":r" + roundNo + ":" + victim + ":" + questionId;
    }

    /** 轮次全部 flag 索引：csp:awd:flag:{contestId}:r{round}（Hash，field=teamId:qid） */
    public static String roundFlags(Long contestId, int roundNo) {
        return P + "flag:" + contestId + ":r" + roundNo;
    }

    /** 单条 flag：csp:awd:flag:{contestId}:r{round}:{teamId}:{qid} */
    public static String flagOne(Long contestId, int roundNo, Long teamId, Long questionId) {
        return P + "flag:" + contestId + ":r" + roundNo + ":" + teamId + ":" + questionId;
    }

    /** 修补记录：csp:awd:patch:{contestId}:r{round}:{teamId}:{qid} */
    public static String patch(Long contestId, int roundNo, Long teamId, Long questionId) {
        return P + "patch:" + contestId + ":r" + roundNo + ":" + teamId + ":" + questionId;
    }

    // ---------------------------------------------------------------- 轮次编排（check / 中断 / 申诉）

    /** 轮次控制标记（Hash）：软中断、挂起等运行时开关。csp:awd:round:ctl:{contestId}:r{round} */
    public static String roundControl(Long contestId, int roundNo) {
        return P + "round:ctl:" + contestId + ":r" + roundNo;
    }

    /** 轮次阶段执行标记（避免重复执行 check/settle）。csp:awd:round:phase:{contestId}:r{round}:{phase} */
    public static String roundPhase(Long contestId, int roundNo, String phase) {
        return P + "round:phase:" + contestId + ":r" + roundNo + ":" + phase;
    }

    /** 某轮某队 check 结果：csp:awd:check:{contestId}:r{round}（Hash，field=teamId，value=JSON） */
    public static String checkResults(Long contestId, int roundNo) {
        return P + "check:" + contestId + ":r" + roundNo;
    }

    /** 某轮某队 check 单项结果：csp:awd:check:{contestId}:r{round}:{teamId}:{qid} */
    public static String checkOne(Long contestId, int roundNo, Long teamId, Long questionId) {
        return P + "check:" + contestId + ":r" + roundNo + ":" + teamId + ":" + questionId;
    }

    /** SLA 扣分（按轮次）：csp:awd:sla:r{round}:{contestId} */
    public static String slaRound(Long contestId, int roundNo) {
        return P + "sla:r" + roundNo + ":" + contestId;
    }

    /** 申诉记录：csp:awd:appeal:{contestId}:r{round}:{appealId} */
    public static String appeal(Long contestId, int roundNo, String appealId) {
        return P + "appeal:" + contestId + ":r" + roundNo + ":" + appealId;
    }

    /** 申诉索引（按轮次，Hash，field=appealId） */
    public static String appeals(Long contestId, int roundNo) {
        return P + "appeals:" + contestId + ":r" + roundNo;
    }
}
