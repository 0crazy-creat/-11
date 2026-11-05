package com.csp.ctf.dto;

import lombok.Data;

/** 参赛报名入参。 */
@Data
public class EnrollCmd {
    /** INDIVIDUAL / TEAM */
    private String mode;
    /** mode=TEAM 时必填：全局战队 id */
    private Long teamId;
}
