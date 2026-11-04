package com.csp.ctf.judge;

/** flag 判题模式，存于 question.ext.judgeMode。 */
public enum JudgeMode {
    /** 静态 flag：题库 flag 字段直接比对 */
    STATIC,
    /** 动态 flag：每队/每实例 flag 由题目模板 + 队伍种子生成，比对生成值 */
    DYNAMIC,
    /** 脚本判题：调用题目自带校验脚本/HTTP 校验器 */
    SCRIPT;

    public static JudgeMode of(String raw) {
        if (raw == null || raw.isBlank()) return STATIC;
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STATIC;
        }
    }
}