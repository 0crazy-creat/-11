package com.csp.ctf.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 战队成员展示信息。 */
@Data
public class TeamMemberVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String orgCode;
    private String orgName;
    /** CAPTAIN/MEMBER */
    private String role;
    private LocalDateTime joinedAt;
}
