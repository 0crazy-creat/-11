package com.csp.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 战队主页（全局战队维度，跨赛事）。
 * <p>数据源：team / team_member / contest_team / submission / writeup，
 * 一次请求返回队伍信息、参赛历史、获奖记录、各场成绩、题解集合与队员能力画像。</p>
 */
@Data
public class TeamProfileVO {

    private Long teamId;
    private String name;
    private String logo;
    private String slogan;
    private String description;
    private Long captainId;
    private String captainName;
    private Integer members;
    private Integer maxMembers;
    private LocalDateTime createdAt;
    /** 当前登录用户在该战队的角色（CAPTAIN/MEMBER/空） */
    private String myRole;

    private Stats stats;
    private List<MemberStat> roster;
    private List<ContestHistory> history;
    private List<Award> awards;
    private List<WriteupBrief> writeups;
    /** 战队整体能力画像：分类 -> 解题数 */
    private Map<String, Integer> abilities;
    /** 战队整体难度画像：难度 -> 解题数 */
    private Map<String, Integer> difficulties;

    /** 汇总指标 */
    @Data
    public static class Stats {
        private int contests;
        private int runningContests;
        private long totalScore;
        private int totalSolves;
        private int totalFirstBloods;
        private Integer bestRank;
        private String bestContest;
        private int writeups;
        private int memberCount;
    }

    /** 成员能力画像（雷达图数据源） */
    @Data
    public static class MemberStat {
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private String orgName;
        /** CAPTAIN/MEMBER */
        private String role;
        private int solves;
        private long score;
        private int firstBloods;
        private int contests;
        private LocalDateTime lastSolveAt;
        /** 分类 -> 解题数 */
        private Map<String, Integer> categories;
        /** 难度 -> 解题数 */
        private Map<String, Integer> difficulties;
    }

    /** 参赛历史与单场成绩 */
    @Data
    public static class ContestHistory {
        /** contest_team.id，赛事内战队主页 /team/{contestTeamId} 用 */
        private Long contestTeamId;
        private Long contestId;
        private String contestName;
        private String contestCode;
        private String type;
        /** 赛事状态 DRAFT/PENDING/SIGNUP/RUNNING/FINISHED/ARCHIVED */
        private String contestStatus;
        /** 报名状态 PENDING/APPROVED/REJECTED/QUIT */
        private String enrollStatus;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer rank;
        private Long score;
        private Integer solved;
        private Integer memberCount;
        private Integer firstBloods;
        private String organizer;
    }

    /** 获奖记录（按最终名次推导） */
    @Data
    public static class Award {
        private Long contestId;
        private String contestName;
        private String type;
        private Integer rank;
        /** 冠军/亚军/季军 */
        private String level;
        private Long score;
        private LocalDateTime endAt;
    }

    /** 题解摘要 */
    @Data
    public static class WriteupBrief {
        private Long id;
        private String title;
        private Long contestId;
        private String contestName;
        private Long questionId;
        private String questionTitle;
        private String category;
        private Long userId;
        private String author;
        private Integer viewCount;
        private Integer likeCount;
        private LocalDateTime createdAt;
    }
}
