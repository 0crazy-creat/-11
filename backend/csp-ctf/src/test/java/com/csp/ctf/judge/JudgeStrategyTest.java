package com.csp.ctf.judge;

import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.score.DynamicScorePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** t7 判题策略 / 动态积分 / 靶机-附件分流 单元验证。 */
class JudgeStrategyTest {

    private CtfQuestion question(String flag, String ext) {
        CtfQuestion q = new CtfQuestion();
        q.setId(1001L);
        q.setBankType("CTF");
        q.setTitle("demo");
        q.setFlag(flag);
        q.setScore(500);
        q.setExt(ext);
        return q;
    }

    private JudgeContext ctx(CtfQuestion q, Long contestId, Long teamId, String submitted) {
        QuestionExt ext = QuestionExt.parse(q.getExt());
        return JudgeContext.builder()
                .contestId(contestId).teamId(teamId).userId(1L)
                .question(q).submitted(submitted).ext(ext)
                .build();
    }

    @Test
    @DisplayName("STATIC：正确 flag 通过，错误 flag 拒绝，flag 为空时回退 answer")
    void staticMode() {
        StaticFlagValidator v = new StaticFlagValidator();
        CtfQuestion q = question("flag{hello_world}", "{\"judgeMode\":\"STATIC\"}");
        assertTrue(v.validate(ctx(q, 1L, 10L, "flag{hello_world}")));
        assertTrue(v.validate(ctx(q, 1L, 10L, "  flag{hello_world}  ")));
        assertFalse(v.validate(ctx(q, 1L, 10L, "flag{wrong}")));

        CtfQuestion byAnswer = question(null, null);
        byAnswer.setAnswer("answer-42");
        assertTrue(v.validate(ctx(byAnswer, 1L, 10L, "answer-42")));
    }

    @Test
    @DisplayName("DYNAMIC：两队 flag 互不相同，且各自只能解自己的")
    void dynamicModeTwoTeams() {
        DynamicFlagValidator v = new DynamicFlagValidator();
        setField(v, "secret", "test-secret");
        setField(v, "prefix", "flag");

        CtfQuestion q = question(null, "{\"judgeMode\":\"DYNAMIC\"}");
        String flagA = v.derive(1L, 10L, 1001L);
        String flagB = v.derive(1L, 11L, 1001L);

        assertNotEquals(flagA, flagB, "不同队伍必须拿到不同 flag");
        assertTrue(flagA.startsWith("flag{") && flagA.endsWith("}"));

        assertTrue(v.validate(ctx(q, 1L, 10L, flagA)), "A 队用自己 flag 应通过");
        assertTrue(v.validate(ctx(q, 1L, 11L, flagB)), "B 队用自己 flag 应通过");
        assertFalse(v.validate(ctx(q, 1L, 10L, flagB)), "A 队用 B 队 flag 必须失败");
        assertFalse(v.validate(ctx(q, 1L, 11L, flagA)), "B 队用 A 队 flag 必须失败");
        assertFalse(v.validate(ctx(q, 1L, null, flagA)), "缺队伍身份必须拒绝");

        assertEquals(flagA, v.derive(1L, 10L, 1001L));
        assertEquals(v.expectedFlag(ctx(q, 1L, 10L, "x")), flagA);
    }

    @Test
    @DisplayName("SCRIPT：未配置校验器时 fail-closed")
    void scriptModeFailClosed() {
        ScriptFlagValidator v = new ScriptFlagValidator();
        CtfQuestion noCfg = question("flag{x}", "{\"judgeMode\":\"SCRIPT\"}");
        assertFalse(v.validate(ctx(noCfg, 1L, 10L, "flag{x}")));

        CtfQuestion badUrl = question("flag{x}",
                "{\"judgeMode\":\"SCRIPT\",\"script\":{\"url\":\"http://127.0.0.1:1/check\",\"timeoutMs\":300}}");
        assertFalse(v.validate(ctx(badUrl, 1L, 10L, "flag{x}")));
    }

    @Test
    @DisplayName("Registry：按 judgeMode 路由，未注册模式回退 STATIC")
    void registryRouting() {
        StaticFlagValidator s = new StaticFlagValidator();
        DynamicFlagValidator d = new DynamicFlagValidator();
        setField(d, "secret", "k");
        setField(d, "prefix", "flag");
        ScriptFlagValidator sc = new ScriptFlagValidator();
        FlagValidatorRegistry reg = new FlagValidatorRegistry(List.of(s, d, sc));

        assertEquals(JudgeMode.STATIC, reg.of(QuestionExt.parse(null)).mode());
        assertEquals(JudgeMode.DYNAMIC, reg.of(QuestionExt.parse("{\"judgeMode\":\"dynamic\"}")).mode());
        assertEquals(JudgeMode.SCRIPT, reg.of(QuestionExt.parse("{\"judgeMode\":\"SCRIPT\"}")).mode());
        assertEquals(JudgeMode.STATIC, reg.of(QuestionExt.parse("{\"judgeMode\":\"NOPE\"}")).mode());
    }

    @Test
    @DisplayName("动态积分：LOG/LINEAR 随解题人数单调衰减，并有下限")
    void dynamicScoreDecay() {
        QuestionExt.DynamicScore log = new QuestionExt.DynamicScore();
        log.setEnabled(true);
        log.setInitial(500);
        log.setMinimum(100);
        log.setDecay(30);
        log.setCurve("LOG");

        int v0 = DynamicScorePolicy.currentValue(log, 500, 0, 20);
        int v1 = DynamicScorePolicy.currentValue(log, 500, 1, 20);
        int v10 = DynamicScorePolicy.currentValue(log, 500, 10, 20);
        int v20 = DynamicScorePolicy.currentValue(log, 500, 20, 20);
        assertEquals(500, v0);
        assertTrue(v0 > v1 && v1 > v10 && v10 >= v20, "LOG 曲线必须单调下降");
        assertEquals(100, v20, "解满全部队伍时落到下限");

        QuestionExt.DynamicScore linear = new QuestionExt.DynamicScore();
        linear.setEnabled(true);
        linear.setInitial(500);
        linear.setMinimum(100);
        linear.setCurve("LINEAR");
        assertEquals(500, DynamicScorePolicy.currentValue(linear, 500, 0, 4));
        assertEquals(400, DynamicScorePolicy.currentValue(linear, 500, 1, 4));
        assertEquals(200, DynamicScorePolicy.currentValue(linear, 500, 3, 4));
        assertEquals(100, DynamicScorePolicy.currentValue(linear, 500, 4, 4));

        assertEquals(500, DynamicScorePolicy.currentValue(null, 500, 10, 20));
    }

    @Test
    @DisplayName("血奖励：一血/二血/三血加成，四血及以后无加成")
    void bloodBonus() {
        QuestionExt.BloodBonus bb = new QuestionExt.BloodBonus();
        bb.setFirst(50);
        bb.setSecond(30);
        bb.setThird(10);
        assertEquals(50, DynamicScorePolicy.bloodBonus(bb, 1));
        assertEquals(30, DynamicScorePolicy.bloodBonus(bb, 2));
        assertEquals(10, DynamicScorePolicy.bloodBonus(bb, 3));
        assertEquals(0, DynamicScorePolicy.bloodBonus(bb, 4));
        assertEquals(0, DynamicScorePolicy.bloodBonus(null, 1));
    }

    @Test
    @DisplayName("实得分数 = 衰减题面分 + 血奖励；两队先后解题分数不同")
    void awardTwoTeams() {
        QuestionExt ext = QuestionExt.parse(
                "{\"judgeMode\":\"STATIC\","
                + "\"dynamicScore\":{\"enabled\":true,\"initial\":500,\"minimum\":100,\"decay\":30,\"curve\":\"LINEAR\"},"
                + "\"bloodBonus\":{\"first\":50,\"second\":30,\"third\":10}}");
        int teamA = DynamicScorePolicy.award(ext, 500, 0, 2, 1);
        int teamB = DynamicScorePolicy.award(ext, 500, 1, 2, 2);
        assertEquals(550, teamA);
        assertEquals(330, teamB);
        assertTrue(teamA > teamB, "先解者必须得分更高");
    }

    @Test
    @DisplayName("ext.isDocker 分流：容器题/附件题识别正确")
    void dockerSplit() {
        QuestionExt docker = QuestionExt.parse(
                "{\"isDocker\":true,\"containerPort\":8080,\"imageName\":\"csp/web:1.0\"}");
        assertTrue(docker.dockerEnabled());
        assertEquals(8080, docker.getContainerPort());

        QuestionExt attach = QuestionExt.parse(
                "{\"isDocker\":false,\"attachmentUrl\":\"/files/rev.zip\"}");
        assertFalse(attach.dockerEnabled());
        assertEquals("/files/rev.zip", attach.getAttachmentUrl());

        QuestionExt empty = QuestionExt.parse(null);
        assertFalse(empty.dockerEnabled());
        assertEquals(JudgeMode.STATIC, empty.mode());
    }

    private static void setField(Object target, String name, Object value) {
        try {
            var f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
