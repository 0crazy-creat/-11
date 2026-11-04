package com.csp.ctf.score;

import com.csp.ctf.judge.QuestionExt;

/**
 * 动态积分衰减策略（CTFd 风格 + Rust-CTF 血奖励）。
 *
 * <p>核心思想：题目分值随解出队伍数增加而衰减，先解者得高分，
 * 避免“后期一窝蜂刷同一道简单题”抬高总榜噪声。</p>
 *
 * <p>两种曲线：</p>
 * <ul>
 *   <li>LINEAR（线性）：value = initial - (initial-min) * solveCount / totalTeams，
 *       掉分匀速，适合参赛队伍数量已知且分布均匀的赛事。</li>
 *   <li>LOG（对数，默认）：value = min + (initial-min) * e^(-k * solveCount / totalTeams)，
 *       前几支队伍掉分最快，之后趋于平缓，贴近真实难度曲线。</li>
 * </ul>
 *
 * <p>最终得分 = 当前题面分值 + 血奖励（一血/二血/三血），结果不小于 0。</p>
 */
public final class DynamicScorePolicy {

    private DynamicScorePolicy() {}

    /**
     * 计算当前题目分值。
     *
     * @param cfg         动态积分配置（null → 视为未启用，返回静态分）
     * @param staticScore 题目静态分值
     * @param solveCount  本次得分之前已解出的队伍数
     * @param totalTeams  参赛队伍总数（至少按 1 处理，避免除零）
     */
    public static int currentValue(QuestionExt.DynamicScore cfg, int staticScore, int solveCount, int totalTeams) {
        if (cfg == null || !cfg.isEnabled()) return staticScore;
        int initial = cfg.getInitial() == null ? Math.max(staticScore, 100) : cfg.getInitial();
        int minimum = cfg.getMinimum() == null ? 0 : cfg.getMinimum();
        if (minimum < 0) minimum = 0;
        if (initial < minimum) initial = minimum;
        int teams = Math.max(1, totalTeams);
        int solved = Math.max(0, solveCount);
        if (solved >= teams) return minimum;

        String curve = cfg.getCurve() == null ? "LOG" : cfg.getCurve().trim().toUpperCase();
        double ratio = (double) solved / teams;
        double value;
        if ("LINEAR".equals(curve)) {
            value = initial - (initial - minimum) * ratio;
        } else {
            int decay = cfg.getDecay() == null ? 30 : Math.max(1, cfg.getDecay());
            // decay 归一化：k = decay/10，ratio=1 时 e^-k 接近 0，由 minimum 兜底
            double k = decay / 10.0;
            value = minimum + (initial - minimum) * Math.exp(-k * ratio);
        }
        long rounded = Math.round(value);
        return (int) Math.max(minimum, Math.min(initial, rounded));
    }

    /**
     * 血奖励：一血/二血/三血分别加成。
     *
     * @param cfg        血奖励配置（null → 无奖励）
     * @param solveOrder 该队是第几个解出（1 = 一血）
     */
    public static int bloodBonus(QuestionExt.BloodBonus cfg, int solveOrder) {
        if (cfg == null || solveOrder <= 0 || solveOrder > 3) return 0;
        return cfg.bonusFor(solveOrder);
    }

    /** 单次解题总得分 = 衰减后题面分 + 血奖励，最低 0。 */
    public static int award(QuestionExt ext, int staticScore, int solveCount, int totalTeams, int solveOrder) {
        QuestionExt.DynamicScore ds = ext == null ? null : ext.getDynamicScore();
        QuestionExt.BloodBonus bb = ext == null ? null : ext.getBloodBonus();
        int base = currentValue(ds, staticScore, solveCount, totalTeams);
        int bonus = bloodBonus(bb, solveOrder);
        return Math.max(0, base + bonus);
    }
}
