package com.csp.ctf.judge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 题目扩展配置（question.ext 的 JSON 映射）。
 *
 * <p>设计参考 GZCTF 的 ChallengeType + Challenge 配置：
 * 把「怎么判题、要不要起容器、分数怎么衰减」全部收敛到一处，
 * 题库表结构保持不变（ext 为 JSON 列），便于各赛制共用同一张 question 表。</p>
 *
 * <pre>
 * {
 *   "challengeType": "DYNAMIC_CONTAINER",  // STATIC_ATTACHMENT / STATIC_CONTAINER / DYNAMIC_CONTAINER
 *   "judgeMode": "STATIC",                 // STATIC / DYNAMIC / SCRIPT（兼容旧数据）
 *   "isDocker": true,                      // 是否动态容器靶机（兼容旧数据）
 *   "containerImage": "csp/web-php:latest",// Docker 镜像（优先于 imageName）
 *   "imageName": "csp/web-php:latest",     // 镜像（兼容旧字段）
 *   "exposePort": 80,                      // 容器内端口
 *   "cpuLimit": 1.0,                       // CPU 限制（核数，0.1 为单位）
 *   "memoryLimit": 256,                    // 内存限制（MB）
 *   "storageLimit": 512,                   // 存储限制（MB）
 *   "networkMode": "ISOLATED",             // 网络模式：OPEN / ISOLATED / CUSTOM
 *   "flagTemplate": "flag{[TEAM_HASH]}",   // 动态 Flag 模板
 *   "attachmentUrl": "...",                // 附件下载地址（非容器题）
 *   "dynamicScore": { ... },               // 动态积分
 *   "bloodBonus": { ... },                 // 血奖励
 *   "script": { ... }                      // 脚本判题
 * }
 * </pre>
 */
@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionExt {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ============ GZCTF 风格的题目类型 ============

    /** 题目类型：STATIC_ATTACHMENT / STATIC_CONTAINER / DYNAMIC_CONTAINER */
    private String challengeType;

    // ============ 兼容旧字段 ============

    /** 判题模式：STATIC / DYNAMIC / SCRIPT（旧版字段，兼容保留） */
    private String judgeMode = "STATIC";

    /** 是否为动态容器靶机题（旧版字段，兼容保留） */
    @com.fasterxml.jackson.annotation.JsonProperty("isDocker")
    private boolean docker;

    // ============ 容器配置（GZCTF 对齐） ============

    /** Docker 镜像名（推荐字段，优先于 imageName） */
    private String containerImage;

    /** Docker 镜像名（兼容旧字段） */
    private String imageName;

    /** 容器内暴露端口 */
    private Integer exposePort = 80;

    /** 容器内端口（兼容旧字段 containerPort） */
    private Integer containerPort;

    /** CPU 限制（核数，如 0.5 表示半核） */
    private Double cpuLimit = 1.0;

    /** 内存限制（MB） */
    private Integer memoryLimit = 256;

    /** 存储限制（MB） */
    private Integer storageLimit = 512;

    /** 网络模式：OPEN / ISOLATED / CUSTOM */
    private String networkMode = "ISOLATED";

    // ============ Flag 配置 ============

    /** 动态 Flag 模板（DYNAMIC_CONTAINER 使用），如 "flag{[TEAM_HASH]}" */
    private String flagTemplate;

    // ============ 附件/脚本 ============

    /** 附件下载地址（非容器题） */
    private String attachmentUrl;

    /** 动态积分配置 */
    private DynamicScore dynamicScore;

    /** 血奖励配置 */
    private BloodBonus bloodBonus;

    /** 脚本判题配置 */
    private Script script;

    // ---------------- 嵌套结构 ----------------

    /** 动态积分衰减配置。 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DynamicScore {
        private boolean enabled;
        /** 初始分值（无人解出时） */
        private Integer initial = 500;
        /** 最低分值（衰减下限） */
        private Integer minimum = 100;
        /** 衰减系数，越大掉分越快 */
        private Integer decay = 30;
        /** 衰减曲线：LINEAR / LOG */
        private String curve = "LOG";
    }

    /** 一血/二血/三血额外奖励。 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BloodBonus {
        private Integer first = 0;
        private Integer second = 0;
        private Integer third = 0;

        public int bonusFor(int solveOrder) {
            return switch (solveOrder) {
                case 1 -> first == null ? 0 : first;
                case 2 -> second == null ? 0 : second;
                case 3 -> third == null ? 0 : third;
                default -> 0;
            };
        }
    }

    /** 脚本判题配置。 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Script {
        private String url;
        private Integer timeoutMs = 2000;
        /** 允许的占位变量：{flag} {submitted} {teamId} {questionId} */
        private List<String> args;
    }

    // ---------------- 解析 ----------------

    /** 解析 ext JSON；空/非法一律回退到安全默认值（静态题）。 */
    public static QuestionExt parse(String json) {
        if (json == null || json.isBlank()) return new QuestionExt();
        try {
            QuestionExt ext = MAPPER.readValue(json, QuestionExt.class);
            return ext == null ? new QuestionExt() : ext;
        } catch (Exception e) {
            log.warn("题目 ext 解析失败，回退 STATIC 模式: {}", e.getMessage());
            return new QuestionExt();
        }
    }

    /** 判题模式枚举（兼容旧数据）。 */
    public JudgeMode mode() {
        return JudgeMode.of(judgeMode);
    }

    /** 动态积分是否生效。 */
    public boolean dynamicScoreEnabled() {
        return dynamicScore != null && dynamicScore.isEnabled();
    }

    // ============ GZCTF 风格的类型判断 ============

    /** 是否为容器题（STATIC_CONTAINER 或 DYNAMIC_CONTAINER）。 */
    public boolean isContainer() {
        if (challengeType != null) {
            return "STATIC_CONTAINER".equals(challengeType) || "DYNAMIC_CONTAINER".equals(challengeType);
        }
        return docker;
    }

    /** 是否为动态题（DYNAMIC_CONTAINER）。 */
    public boolean isDynamic() {
        if (challengeType != null) {
            return "DYNAMIC_CONTAINER".equals(challengeType);
        }
        return "DYNAMIC".equalsIgnoreCase(judgeMode);
    }

    /** 是否为静态题。 */
    public boolean isStatic() {
        return !isDynamic();
    }

    /** 是否走容器靶机（兼容旧方法名）。 */
    public boolean dockerEnabled() {
        return isContainer();
    }

    /** 获取有效的镜像名（containerImage 优先于 imageName）。 */
    public String getEffectiveImage() {
        return containerImage != null && !containerImage.isBlank() ? containerImage : imageName;
    }

    /** 获取有效的容器端口（exposePort 优先于 containerPort）。 */
    public int getEffectivePort() {
        return exposePort != null ? exposePort : (containerPort != null ? containerPort : 80);
    }
}